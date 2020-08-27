package xyz.nucleoid.plasmid.mixin.chat;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.translation.TranslationHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @ModifyArg(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"), index = 0)
    private Packet<?> sendCorrectPacket(Packet<?> packet) {
        if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket oldPacket = (GameMessageS2CPacket) packet;
            Text message = ((GameMessageS2CPacketAccessor)oldPacket).getText();
            if (message instanceof TranslatableText) packet = new GameMessageS2CPacket(TranslationHandler.getCorrectText((TranslatableText) message, this.player),
                    oldPacket.getLocation(), ((GameMessageS2CPacketAccessor)oldPacket).getUUID());
        }
        if (packet instanceof TitleS2CPacket) {
            TitleS2CPacketAccessor titleS2CPacket = (TitleS2CPacketAccessor)packet;
            Text message = titleS2CPacket.getMessage();
            if (message instanceof TranslatableText) {
                packet = new TitleS2CPacket(titleS2CPacket.getTitleAction(),
                        TranslationHandler.getCorrectText((TranslatableText) message, this.player),
                        titleS2CPacket.getFadeIn(), titleS2CPacket.getStay(), titleS2CPacket.getFadeOut());
            }
        }
        return packet;
    }
}
