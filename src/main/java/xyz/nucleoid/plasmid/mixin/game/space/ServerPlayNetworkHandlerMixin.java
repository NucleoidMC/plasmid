package xyz.nucleoid.plasmid.mixin.game.space;

import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onClientStatus", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;notInAnyWorld:Z", ordinal = 1), cancellable = true)
    void onPerformRespawn(ClientStatusC2SPacket packet, CallbackInfo ci)
    {
        var gameSpace = GameSpaceManager.get().byPlayer(this.player);

        if(gameSpace == null) return;
        if(gameSpace.getWorlds().contains(this.player.getSpawnPointDimension())) return;

        gameSpace.getPlayers().kick(this.player);
        ci.cancel(); //this player shouldn't be respawned but kicked or get his old instance back
    }
}
