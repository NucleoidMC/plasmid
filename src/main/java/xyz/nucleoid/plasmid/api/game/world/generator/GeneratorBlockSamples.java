package xyz.nucleoid.plasmid.api.game.world.generator;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import java.util.Arrays;

public final class GeneratorBlockSamples {
    public static final VerticalBlockSample VOID = new VerticalBlockSample(0, new BlockState[0]);

    public static VerticalBlockSample heightmap(int minY, int maxY, BlockState block) {
        int height = maxY - minY + 1;

        var sample = new BlockState[height];
        Arrays.fill(sample, block);

        return new VerticalBlockSample(minY, sample);
    }
}
