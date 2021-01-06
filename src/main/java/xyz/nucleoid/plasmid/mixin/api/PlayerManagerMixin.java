package xyz.nucleoid.plasmid.mixin.api;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.network.Networking;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ServerPlayNetworking.send(player, Networking.IS_USING_PLASMID_API, PacketByteBufs
                .create()
                .writeString(Plasmid.METADATA.getVersion().getFriendlyString())
        );
    }
}
