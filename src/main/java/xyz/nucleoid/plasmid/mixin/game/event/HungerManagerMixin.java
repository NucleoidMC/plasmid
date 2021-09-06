package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.PlayerRegenerateListener;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void attemptRegeneration(PlayerEntity player, CallbackInfo ci, Difficulty difficulty, boolean naturalRegeneration, float amount) {
        if (player.world.isClient) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) (Object) player;

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(serverPlayer.world);
        if (gameSpace != null && gameSpace.containsPlayer(serverPlayer)) {
            try {
                ActionResult result = gameSpace.invoker(PlayerRegenerateListener.EVENT).onRegenerate(serverPlayer, amount);
                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player regenerate event", t);
                gameSpace.reportError(t, "Player regenerating");
            }
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", shift = At.Shift.BEFORE, ordinal = 1), cancellable = true)
    private void attemptSecondaryRegeneration(PlayerEntity player, CallbackInfo ci) {
        if (player.world.isClient) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) (Object) player;

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(serverPlayer.world);
        if (gameSpace != null && gameSpace.containsPlayer(serverPlayer)) {
            try {
                ActionResult result = gameSpace.invoker(PlayerRegenerateListener.EVENT).onRegenerate(serverPlayer, 1);
                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player regenerate event", t);
                gameSpace.reportError(t, "Player regenerating");
            }
        }
    }
}
