package xyz.nucleoid.plasmid.game.gen.feature;

import java.util.Random;

import xyz.nucleoid.plasmid.game.gen.GenHelper;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;

public class PoplarTreeGen implements MapGen {
    public static final PoplarTreeGen INSTANCE = new PoplarTreeGen(Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1));

    private final BlockState log;
    private final BlockState leaves;

    public PoplarTreeGen(BlockState log, BlockState leaves) {
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
        if (world.getBlockState(pos.down()) != Blocks.GRASS_BLOCK.getDefaultState()) return;

        double maxRadius = 2.6 + ((random.nextDouble() - 0.5) * 0.2);
        int leafDistance = random.nextInt(3) + 2;

        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int y = 0; y < 12; y++) {
            world.setBlockState(mutable, log, 0);
            //add branch blocks
            if (maxRadius * radius(y / 11.f) > 2.3) {
                Direction.Axis axis = getAxis(random);
                world.setBlockState(mutable.offset(getDirection(axis, random)).up(leafDistance), log.with(Properties.AXIS, axis), 0);
            }

            mutable.move(Direction.UP);
        }

        mutable = pos.mutableCopy();
        mutable.move(Direction.UP, leafDistance);

        for (int y = 0; y < 12; y++) {
            GenHelper.circle(mutable.mutableCopy(), maxRadius * radius(y / 11.f), leafPos -> {
                if (world.getBlockState(leafPos).isAir()) {
                    world.setBlockState(leafPos, leaves, 0);
                }
            });
            mutable.move(Direction.UP);
        }
    }

    private double radius(double x) {
        return (-2 * (x * x * x)) + (1.9 * x) + 0.2;
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