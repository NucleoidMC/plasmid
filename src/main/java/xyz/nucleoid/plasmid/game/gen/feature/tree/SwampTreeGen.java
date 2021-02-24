package xyz.nucleoid.plasmid.game.gen.feature.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import xyz.nucleoid.plasmid.game.gen.GenHelper;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import java.util.Random;

public final class SwampTreeGen implements MapGen {
    public static final SwampTreeGen INSTANCE = new SwampTreeGen(Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1));
    private final BlockState log;
    private final BlockState leaves;

    public SwampTreeGen(BlockState log, BlockState leaves) {
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
        if (world.getBlockState(pos.down()) != Blocks.GRASS_BLOCK.getDefaultState()) return;

        double maxRadius = 1 + ((random.nextDouble() - 0.5) * 0.2);
        int leafDistance = random.nextInt(3) + 3;

        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int y = 0; y < 6; y++) {
            world.setBlockState(mutable, this.log, 3);

            mutable.move(Direction.UP);
        }

        mutable = pos.mutableCopy();
        mutable.move(Direction.UP, leafDistance);

        for (int y = 0; y < 5; y++) {
            GenHelper.circle(mutable.mutableCopy(), maxRadius * this.radius(y / 5.f), leafPos -> {
                if (world.getBlockState(leafPos).isAir()) {
                    world.setBlockState(leafPos, this.leaves, 0);
                }
            });

            mutable.move(Direction.UP);
        }
    }

    private double radius(double x) {
        return Math.max((-2.3 * (x * x * x)) + 2.5, 0);
    }
}
