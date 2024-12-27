package xyz.nucleoid.plasmid.mixin.game.rule;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.stimuli.event.EventResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootTable.class)
public class LootTableMixin {
    @WrapWithCondition(
            method = "supplyInventory",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;shuffle(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;ILnet/minecraft/util/math/random/Random;)V")
    )
    public boolean preventContainerLootShuffling(LootTable lootTable, ObjectArrayList<ItemStack> stacks, int freeSlots, Random random, @Local LootContext context) {
        var entity = context.get(LootContextParameters.THIS_ENTITY);
        var gameSpace = entity instanceof ServerPlayerEntity player ? GameSpaceManagerImpl.get().byPlayer(player) : GameSpaceManagerImpl.get().byWorld(context.getWorld());

        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SPREAD_CONTAINER_LOOT) == EventResult.DENY) {
            return false;
        }

        return true;
    }
}
