package xyz.nucleoid.plasmid.game.gen.feature.tree;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class DeadTreeGen implements MapGen {
	public static final DeadTreeGen INSTANCE = new DeadTreeGen(Blocks.OAK_LOG.getDefaultState(), 13, 4);
	private final BlockState log;
	private final int baseHeight;
	private final int randomHeight;

	public DeadTreeGen(BlockState log, int baseHeight, int randomHeight) {
		this.log = log;
		this.baseHeight = baseHeight;
		this.randomHeight = randomHeight;
	}

	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		if (!(world.getBlockState(pos).isAir() && (world.getBlockState(pos.down()).isOf(Blocks.SAND) || world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK)))) {
			return;
		}

		int trunkHeight = random.nextInt(this.randomHeight) + this.baseHeight;
		int scaledTrunkHeight = MathHelper.floor((double) trunkHeight * 0.618D);

		int branchCount = Math.min(1, MathHelper.floor(1.382D + Math.pow(1.0D * (double) trunkHeight / 13.0D, 2.0D))); // wtf even is this lol
		int maxExtent = pos.getY() + scaledTrunkHeight;
		int yProgress = trunkHeight - 5;
		List<BranchPosition> list = Lists.newArrayList();
		list.add(new BranchPosition(pos.up(yProgress), maxExtent));

		for(; yProgress >= 0; --yProgress) {
			float heightProgress = this.getHeightProgress(trunkHeight, yProgress);

			if (heightProgress >= 0.0F) {
				for(int o = 0; o < branchCount; ++o) {
					double branchDirection = 1.0D * (double) heightProgress * ((double) random.nextFloat() + 0.328D);
					double randomTheta = (double) (random.nextFloat() * 2.0F) * 3.141592653589793D;
					double localX = branchDirection * Math.sin(randomTheta) + 0.5D;
					double localZ = branchDirection * Math.cos(randomTheta) + 0.5D;
					BlockPos local = pos.add(localX, (yProgress - 1), localZ);
					BlockPos upperLocal = local.up(5);

					if (this.makeOrCheckBranch(world, random, local, upperLocal, false)) {
						int branchX = pos.getX() - local.getX();
						int branchZ = pos.getZ() - local.getZ();
						double branchLength = (double) local.getY() - Math.sqrt(branchX * branchX + branchZ * branchZ) * 0.381D;
						int finalBranchLength = branchLength > (double) maxExtent ? maxExtent : (int) branchLength;
						BlockPos endPos = new BlockPos(pos.getX(), finalBranchLength, pos.getZ());
						if (this.makeOrCheckBranch(world, random, endPos, local, false)) {
							list.add(new BranchPosition(local, endPos.getY()));
						}
					}
				}
			}
		}

		this.makeOrCheckBranch(world, random, pos, pos.up(scaledTrunkHeight), true);
		this.makeBranches(world, random, trunkHeight, pos, list);
	}

	private boolean makeOrCheckBranch(ModifiableTestableWorld world, Random random, BlockPos start, BlockPos end, boolean make) {
		if (make || !Objects.equals(start, end)) {
			BlockPos blockPos = end.add(-start.getX(), -start.getY(), -start.getZ());
			int longestSide = this.getLongestSide(blockPos);
			float sideX = (float) blockPos.getX() / (float) longestSide;
			float sideY = (float) blockPos.getY() / (float) longestSide;
			float sideZ = (float) blockPos.getZ() / (float) longestSide;

			for (int side = 0; side <= longestSide; ++side) {
				BlockPos blockPos2 = start.add(0.5F + (float) side * sideX, 0.5F + (float) side * sideY, 0.5F + (float) side * sideZ);
				if (make) {
					world.setBlockState(blockPos2, this.log.with(PillarBlock.AXIS, this.getLogAxis(start, blockPos2)), 3);
				} else if (!TreeFeature.canTreeReplace(world, blockPos2)) {
					return false;
				}
			}

		}

		return true;
	}

	private float getHeightProgress(int trunkHeight, int branchCount) {
		if ((float)branchCount < (float)trunkHeight * 0.3F) {
			return -1.0F;
		} else {
			float scaledTrunkHeight = (float)trunkHeight / 2.0F;
			float scaledBranches = scaledTrunkHeight - (float)branchCount;
			float progress = MathHelper.sqrt(scaledTrunkHeight * scaledTrunkHeight - scaledBranches * scaledBranches);
			if (scaledBranches == 0.0F) {
				progress = scaledTrunkHeight;
			} else if (Math.abs(scaledBranches) >= scaledTrunkHeight) {
				return 0.0F;
			}

			return progress * 0.5F;
		}
	}

	private boolean isHighEnough(int treeHeight, int height) {
		return (double)height >= (double)treeHeight * 0.2D;
	}

	private int getLongestSide(BlockPos offset) {
		int x = MathHelper.abs(offset.getX());
		int y = MathHelper.abs(offset.getY());
		int z = MathHelper.abs(offset.getZ());
		return Math.max(x, Math.max(y, z));
	}

	private Direction.Axis getLogAxis(BlockPos branchStart, BlockPos branchEnd) {
		Direction.Axis axis = Direction.Axis.Y;
		int scaledX = Math.abs(branchEnd.getX() - branchStart.getX());
		int scaledZ = Math.abs(branchEnd.getZ() - branchStart.getZ());
		int maxAxis = Math.max(scaledX, scaledZ);

		if (maxAxis > 0) {
			if (scaledX == maxAxis) {
				axis = Direction.Axis.X;
			} else {
				axis = Direction.Axis.Z;
			}
		}

		return axis;
	}

	private void makeBranches(ModifiableTestableWorld world, Random random, int treeHeight, BlockPos treePos, List<BranchPosition> branches) {
		for (BranchPosition branchPosition : branches) {
			int endY = branchPosition.getEndY();
			BlockPos startPos = new BlockPos(treePos.getX(), endY, treePos.getZ());
			if (!startPos.equals(branchPosition.node.getCenter()) && this.isHighEnough(treeHeight, endY - treePos.getY())) {
				this.makeOrCheckBranch(world, random, startPos, branchPosition.node.getCenter(), true);
			}
		}

	}

	static class BranchPosition {
		private final FoliagePlacer.TreeNode node;
		private final int endY;

		public BranchPosition(BlockPos pos, int endY) {
			this.node = new FoliagePlacer.TreeNode(pos, 0, false);
			this.endY = endY;
		}

		public int getEndY() {
			return this.endY;
		}
	}
}
