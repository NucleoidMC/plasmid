package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class BucketFind {
    private BucketFind() {
    }

    /**
     * Finds any directly connected blocks and puts them in a set.
     *
     * @param origin the position of the first block
     * @param depth the amount of maximum blocks to find
     * @param block the block type that can be accepted in the set
     */
    public static Set<BlockPos> find(ServerWorld world, BlockPos origin, int depth, Block block) {
        return find(world, origin, depth, state -> state.isOf(block));
    }

    /**
     * Finds any directly connected blocks and puts them in a set.
     *
     * @param origin the position of the first block
     * @param depth the amount of maximum blocks to find
     * @param tag the tag for the blocks that can be accepted in the set
     */
    public static Set<BlockPos> find(ServerWorld world, BlockPos origin, int depth, Tag<Block> tag) {
        return find(world, origin, depth, state -> state.isIn(tag));
    }

    /**
     * Finds any directly connected blocks and puts them in a set.
     *
     * @param origin the position of the first block
     * @param depth the amount of maximum blocks to find
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static Set<BlockPos> find(ServerWorld world, BlockPos origin, int depth, Predicate<BlockState> predicate) {
        Set<BlockPos> set = new HashSet<>();
        Set<BlockPos> ends = new HashSet<>();
        ends.add(origin);
        while (depth > 0) {
            if (ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.stream().findAny().get();
            for (Direction direction : Direction.values()) {
                BlockPos.Mutable local = pos.offset(direction).mutableCopy();
                BlockState state = world.getBlockState(local);
                if (predicate.test(state)) {
                    if (!set.contains(local)) {
                        ends.add(local);
                    }
                }
            }
            set.add(pos);
            ends.remove(pos);
            depth--;
        }
        return set;
    }
}