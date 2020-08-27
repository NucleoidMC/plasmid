package xyz.nucleoid.plasmid.mixin.chat;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.translation.TranslationHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    private void sendCorrectPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket oldPacket = (GameMessageS2CPacket) packet;
            Text message = ((GameMessageS2CPacketAccessor)oldPacket).getText();
            if (message instanceof TranslatableText) packet = new GameMessageS2CPacket(TranslationHandler.getCorrectText((TranslatableText) message, this.player),
                    oldPacket.getLocation(), ((GameMessageS2CPacketAccessor)oldPacket).getUUID());
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
    private void sendCorrectPacket2(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket oldPacket = (GameMessageS2CPacket) packet;
            Text message = ((GameMessageS2CPacketAccessor)oldPacket).getText();
            if (message instanceof TranslatableText) packet = new GameMessageS2CPacket(TranslationHandler.getCorrectText((TranslatableText) message, this.player),
                    oldPacket.getLocation(), ((GameMessageS2CPacketAccessor)oldPacket).getUUID());
        }
    }
}
