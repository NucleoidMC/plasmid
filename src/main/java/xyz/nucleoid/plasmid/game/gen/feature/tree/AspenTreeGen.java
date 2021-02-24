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

public final class AspenTreeGen implements MapGen {
    public static final AspenTreeGen INSTANCE = new AspenTreeGen(Blocks.BIRCH_LOG.getDefaultState(), Blocks.BIRCH_LEAVES.getDefaultState());
    private final BlockState log;
    private final BlockState leaves;

    public AspenTreeGen(BlockState log, BlockState leaves) {
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {

        double maxRadius = 2 + ((random.nextDouble() - 0.5) * 0.2);
        int leafDistance = random.nextInt(4) + 3;
        BlockPos.Mutable mutable = pos.mutableCopy();

        for (int y = 0; y < 8; y++) {
            world.setBlockState(mutable, this.log, 3);
            // Add branch blocks
            if (maxRadius * this.radius(y / 7.f) > 2.1) {
                Direction.Axis axis = this.getAxis(random);
                world.setBlockState(mutable.offset(this.getDirection(axis, random)).up(leafDistance), this.log.with(Properties.AXIS, axis), 3);
            }

            mutable.move(Direction.UP);
        }

        mutable = pos.mutableCopy();
        mutable.move(Direction.UP, leafDistance);
        for (int y = 0; y < 8; y++) {
            GenHelper.circle(mutable.mutableCopy(), maxRadius * this.radius(y / 7.f), leafPos -> {
                if (world.getBlockState(leafPos).isAir()) {
                    world.setBlockState(leafPos, this.leaves, 3);
                }
            });

            mutable.move(Direction.UP);
        }
    }

    private double radius(double x) {
        return -Math.pow(((1.4 * x) - 0.3), 2) + 1.2;
    }

    private Direction.Axis getAxis(Random random) {
        return random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
    }

    private Direction getDirection(Direction.Axis axis, Random random) {
        if (axis == Direction.Axis.X) {
            return random.nextBoolean() ? Direction.EAST : Direction.WEST;
        } else {
            return random.nextBoolean() ? Direction.NORTH : Direction.SOUTH;
        }
    }
}
