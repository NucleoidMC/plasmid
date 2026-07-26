package xyz.nucleoid.plasmid.mixin.game.rule;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.stimuli.event.EventResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootTable.class)
public class LootTableMixin {
    @WrapWithCondition(
            method = "fill",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;shuffleAndSplitItems(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;ILnet/minecraft/util/RandomSource;)V")
    )
    public boolean preventContainerLootShuffling(LootTable lootTable, ObjectArrayList<ItemStack> stacks, int freeSlots, RandomSource random, @Local LootContext context) {
        var entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        var gameSpace = entity instanceof ServerPlayer player ? GameSpaceManagerImpl.get().byPlayer(player) : GameSpaceManagerImpl.get().byLevel(context.getLevel());

        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SPREAD_CONTAINER_LOOT) == EventResult.DENY) {
            return false;
        }

        return true;
    }
}
