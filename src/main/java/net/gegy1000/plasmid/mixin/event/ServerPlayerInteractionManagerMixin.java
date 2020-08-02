package net.gegy1000.plasmid.mixin.event;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.GameManager;
import net.gegy1000.plasmid.game.event.BreakBlockListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (this.player.world.isClient) {
            return;
        }

        Game game = GameManager.openGame();
        if (game != null && game.containsPos(pos)) {
            boolean cancel = game.invoker(BreakBlockListener.EVENT).onBreak(game, this.player, pos);
            if (cancel) {
                ci.setReturnValue(false);
            }
        }
    }
}
