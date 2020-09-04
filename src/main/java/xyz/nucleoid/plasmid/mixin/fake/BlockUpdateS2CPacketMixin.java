package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.plasmid.fake.FakeBlock;

@Mixin({ BlockUpdateS2CPacket.class, ChunkDeltaUpdateS2CPacket.class })
public class BlockUpdateS2CPacketMixin {
    @ModifyArg(
            method = "write",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I")
    )
    private BlockState modifyBlockState(BlockState state) {
        return FakeBlock.getProxy(state);
    }
}
