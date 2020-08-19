package xyz.nucleoid.plasmid.game.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import java.util.Random;

public final class ShrubGen implements MapGen {
	public static final ShrubGen INSTANCE = new ShrubGen(Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1));
	private final BlockState log;
	private final BlockState leaves;

	public ShrubGen(BlockState log, BlockState leaves) {
		this.log = log;
		this.leaves = leaves;
	}

	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		if (world.getBlockState(pos.down()) != Blocks.GRASS_BLOCK.getDefaultState()) return;

		world.setBlockState(pos, this.log, 3);

		if (random.nextBoolean()) {
			pos = pos.up();
			world.setBlockState(pos, this.log, 3);
		}

		for (Direction dir : Direction.values()) {
			BlockPos local = pos.offset(dir);

			if (world.getBlockState(local).isAir()) {
				world.setBlockState(local, this.leaves, 3);
			}
		}
	}
}
