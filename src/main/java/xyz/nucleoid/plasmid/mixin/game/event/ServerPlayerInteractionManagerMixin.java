package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (this.player.world.isClient) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.player.world);
        if (gameSpace != null && gameSpace.containsPlayer(this.player)) {
            try {
                ActionResult result = gameSpace.invoker(BreakBlockListener.EVENT).onBreak(this.player, pos);

                if (result == ActionResult.FAIL) {
                    ci.setReturnValue(false);
                }
            } catch (Exception e) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching block break event", e);
            }
        }
    }
}
