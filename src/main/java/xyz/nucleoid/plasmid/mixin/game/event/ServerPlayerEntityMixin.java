package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.GameWorld;
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

        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null && gameWorld.containsPlayer(player)) {
            ActionResult result = gameWorld.invoker(PlayerDeathListener.EVENT).onDeath(player, source);

            if (result == ActionResult.FAIL) {
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
