package xyz.nucleoid.plasmid.mixin.fake;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow
    @Final
    private NetworkSide side;

    @Shadow
    private Channel channel;

    @Shadow
    public abstract boolean isLocal();

    /**
     * Here we ensure that packets sent on the integrated server are serialized/deserialized. This is usually skipped
     * for optimization purposes. In the case of faking items, however, we depend on the ability to hook and swap
     * values as they are sent to the client. Unfortunately, a more "proper" solution would involve being much more
     * invasive and introduce challenges relating to integrating with other mods.
     */
    @ModifyVariable(method = "sendImmediately", at = @At("HEAD"), argsOnly = true, index = 1)
    private Packet<?> modify(Packet<?> packet) throws IOException {
        if (!this.isLocal() || this.side != NetworkSide.CLIENTBOUND) {
            return packet;
        }

        PacketByteBuf buffer = new PacketByteBuf(this.channel.alloc().buffer());
        try {
            packet.write(buffer);
            buffer.resetReaderIndex();
            packet.read(buffer);
        } finally {
            buffer.release();
        }

        return packet;
    }
}
