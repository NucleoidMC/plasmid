package xyz.nucleoid.plasmid.item;

import eu.pb4.polymer.item.ItemHelper;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;

public final class IncludeEntityItem extends Item implements VirtualItem {
    public IncludeEntityItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getEntityWorld();
        if (!world.isClient) {
            MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(world.getServer());

            MapWorkspace workspace = workspaceManager.byDimension(world.getRegistryKey());
            if (workspace != null) {
                if (!workspace.getBounds().contains(entity.getBlockPos())) {
                    user.sendMessage(
                            new TranslatableText(stack.getTranslationKey() + ".target_not_in_map", workspace.getIdentifier())
                                    .formatted(Formatting.RED),
                            false);
                    return ActionResult.FAIL;
                }

                if (workspace.containsEntity(entity.getUuid())) {
                    workspace.removeEntity(entity.getUuid());
                    user.sendMessage(
                            new TranslatableText(stack.getTranslationKey() + ".removed", workspace.getIdentifier()),
                            true);
                } else {
                    workspace.addEntity(entity.getUuid());
                    user.sendMessage(
                            new TranslatableText(stack.getTranslationKey() + ".added", workspace.getIdentifier()),
                            true);
                }
                return ActionResult.SUCCESS;
            } else {
                user.sendMessage(new TranslatableText(stack.getTranslationKey() + ".player_not_in_map").formatted(Formatting.RED),
                        false);
                return ActionResult.FAIL;
            }
        }

        return ActionResult.FAIL;
    }

    @Override
    public Item getVirtualItem() {
        return Items.LEAD;
    }

    @Override
    public ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        ItemStack virtual = ItemHelper.createBasicVirtualItemStack(itemStack, player);
        virtual.addEnchantment(Enchantments.POWER, 1);
        return virtual;
    }
}
