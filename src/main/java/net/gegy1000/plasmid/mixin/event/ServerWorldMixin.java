package net.gegy1000.plasmid.mixin.event;

import net.gegy1000.plasmid.game.GameWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void onPlayerAdded(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null) {
            gameWorld.addPlayer(player);
        }
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void onPlayerRemoved(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null) {
            gameWorld.removePlayer(player);
        }
    }
}
