package xyz.nucleoid.plasmid.mixin.map;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.map.creation.workspace.MapWorkspaceManager;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void addPlayer(ServerPlayerEntity player, CallbackInfo ci) {
        MapWorkspaceManager.get(player.server).onPlayerAddToWorld(player, (ServerWorld) (Object) this);
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void removePlayer(ServerPlayerEntity player, Entity.RemovalReason reason, CallbackInfo ci) {
        MapWorkspaceManager.get(player.server).onPlayerRemoveFromWorld(player, (ServerWorld) (Object) this);
    }
}
