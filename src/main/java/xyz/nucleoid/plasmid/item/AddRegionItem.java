package xyz.nucleoid.plasmid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.fake.FakeItem;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;
import xyz.nucleoid.plasmid.map.workspace.editor.WorkspaceEditor;

public final class AddRegionItem extends Item implements FakeItem {
    public AddRegionItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            return super.use(world, player, hand);
        }

        ItemStack stack = player.getStackInHand(hand);

        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(serverPlayer.server);
            WorkspaceEditor editor = workspaceManager.getEditorFor(serverPlayer);

            if (editor != null && editor.useRegionItem()) {
                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public Item asProxy() {
        return Items.STICK;
    }
}
