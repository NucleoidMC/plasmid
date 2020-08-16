package xyz.nucleoid.plasmid.game.gen.feature;

import java.util.Random;

import xyz.nucleoid.plasmid.game.gen.MapGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;

public class GrassGen implements MapGen {
	public static final GrassGen INSTANCE = new GrassGen(new WeightedList<BlockState>()
			.add(Blocks.GRASS.getDefaultState(), 32)
			.add(Blocks.DANDELION.getDefaultState(), 1)
			.add(Blocks.POPPY.getDefaultState(), 1));
	private final WeightedList<BlockState> states;

	public GrassGen(WeightedList<BlockState> states) {
		this.states = states;
	}

	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		BlockState state = states.pickRandom(random);

		for (int i = 0; i < 16; i++) {
			int aX = random.nextInt(8) - random.nextInt(8);
			int aY = random.nextInt(4) - random.nextInt(4);
			int aZ = random.nextInt(8) - random.nextInt(8);
			BlockPos local = pos.add(aX, aY, aZ);

			if (world.getBlockState(local.down()) == Blocks.GRASS_BLOCK.getDefaultState() && world.getBlockState(local).isAir()) {
				world.setBlockState(local, state, 3);
			}
		}
	}
}