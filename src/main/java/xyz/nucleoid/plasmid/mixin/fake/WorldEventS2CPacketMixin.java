package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.plasmid.fake.FakeBlock;

@Mixin(WorldEventS2CPacket.class)
public class WorldEventS2CPacketMixin {
    @Shadow private int eventId;

    @ModifyArg(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lio/netty/buffer/ByteBuf;",
                    ordinal = 1
            )
    )
    private int modifyBlockData(int data) {
        if (this.eventId == 2001) {
            BlockState state = Block.getStateFromRawId(data);
            state = FakeBlock.getProxy(state);
            return Block.getRawIdFromState(state);
        }
        return data;
    }
}
