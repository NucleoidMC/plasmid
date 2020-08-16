package xyz.nucleoid.plasmid.mixin.rule;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void shouldDropItems(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (this.player.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(this.player.world);
        if (gameWorld != null && gameWorld.containsPlayer(this.player)) {
            RuleResult result = gameWorld.testRule(GameRule.BLOCK_DROPS);
            if (result == RuleResult.DENY) {
                ci.setReturnValue(true);
            }
        }
    }
}
