package net.gegy1000.plasmid.mixin.event;

import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.event.PlayerDamageListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null && gameWorld.containsPlayer(player)) {
            boolean cancel = gameWorld.invoker(PlayerDeathListener.EVENT).onDeath(player, source);
            if (cancel) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null && gameWorld.containsPlayer(player)) {
            boolean cancel = gameWorld.invoker(PlayerDamageListener.EVENT).onDamage(player, source, amount);
            if (cancel) {
                ci.cancel();
            }
        }
    }
}
