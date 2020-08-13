package xyz.nucleoid.plasmid.mixin.fake;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow
    private Channel channel;

    @Shadow
    public abstract boolean isLocal();

    @ModifyVariable(method = "sendImmediately", at = @At("HEAD"), argsOnly = true, index = 1)
    private Packet<?> modify(Packet<?> packet) throws IOException {
        if (!isLocal()) {
            return packet;
        }

        PacketByteBuf buffer = new PacketByteBuf(channel.alloc().buffer());
        packet.write(buffer);
        packet.read(buffer);

        return packet;
    }
}
