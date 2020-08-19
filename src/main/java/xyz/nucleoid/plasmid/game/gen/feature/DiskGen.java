package xyz.nucleoid.plasmid.game.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import java.util.Random;

public final class DiskGen implements MapGen {
    public static final DiskGen INSTANCE = new DiskGen(new WeightedList<BlockState>()
            .add(Blocks.SAND.getDefaultState(), 1)
            .add(Blocks.GRAVEL.getDefaultState(), 1), 2, 5);
    private final WeightedList<BlockState> states;
    private final int baseSize;
    private final int randomSize;

    public DiskGen(WeightedList<BlockState> states, int baseSize, int randomSize) {
        this.states = states;
        this.baseSize = baseSize;
        this.randomSize = randomSize;
    }

    @Override
    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {

        int radius = random.nextInt(this.randomSize) + this.baseSize;
        int radiusSquared = radius * radius;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockState state = this.states.pickRandom(random);

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; ++x) {
            for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; ++z) {
                int localX = x - pos.getX();
                int localZ = z - pos.getZ();
                if (localX * localX + localZ * localZ <= radiusSquared) {
                    for (int y = pos.getY() - 2; y <= pos.getY() + 2; ++y) {
                        mutable.set(x, y, z);

                        if (world.getBlockState(mutable).isOf(Blocks.DIRT) || world.getBlockState(mutable).isOf(Blocks.GRASS_BLOCK)) {
                            world.setBlockState(mutable, state, 3);

                            if (!world.getBlockState(mutable.up()).canPlaceAt(world, mutable)) {
                                world.setBlockState(mutable.up(), Blocks.AIR.getDefaultState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
}
