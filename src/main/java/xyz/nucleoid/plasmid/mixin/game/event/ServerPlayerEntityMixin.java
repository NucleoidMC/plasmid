package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.world.isClient) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(player.world);
        if (gameSpace != null && gameSpace.containsPlayer(player)) {
            try {
                ActionResult result = gameSpace.invoker(PlayerDeathListener.EVENT).onDeath(player, source);

                if (result == ActionResult.FAIL) {
                    if (player.getHealth() <= 0.0F) {
                        player.setHealth(20.0F);
                    }
                    ci.cancel();
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player death event", t);
                gameSpace.reportError(t, "Player death");
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.world.isClient) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(player.world);
        if (gameSpace != null && gameSpace.containsPlayer(player)) {
            try {
                ActionResult result = gameSpace.invoker(PlayerDamageListener.EVENT).onDamage(player, source, amount);
                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player damage event", t);
                gameSpace.reportError(t, "Player damage");
            }
        }
    }
}
