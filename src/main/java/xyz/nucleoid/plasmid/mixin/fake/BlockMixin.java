package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.fake.FakeBlock;

@Mixin(Block.class)
public class BlockMixin {

    @ModifyVariable(method = "getRawIdFromState", at = @At("HEAD"), argsOnly = true, index = 0)
    private static BlockState modify(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof FakeBlock) {
            return ((FakeBlock<?>) block).getFaking(state);
        } else {
            return state;
        }
    }
}
