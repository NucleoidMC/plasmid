package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Methods for finding connected blocks as a {@link LongSet}. Use {@link BlockPos#fromLong} to retrieve these positions.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
 */
public final class BucketFind {
    private BucketFind() {
    }

    /**
     * Finds any 6-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin    the position of the first block
     * @param depth     the amount of maximum blocks to find
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static LongSet findSix(ServerWorld world, BlockPos origin, int depth, Predicate<BlockState> predicate) {
        LongSet set = new LongArraySet();
        Deque<BlockPos> ends = new ArrayDeque<>();
        ends.push(origin);
        BlockPos.Mutable local = origin.mutableCopy();
        while(depth > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.pollLast();
            for(Direction direction : Direction.values()) {
                local.set(pos).offset(direction);
                if(predicate.test(world.getBlockState(local)) && !set.contains(local.asLong()) && !ends.contains(local)) {
                    ends.push(local.toImmutable());
                }
            }
            set.add(pos.asLong());
            depth--;
        }
        return set;
    }

    /**
     * Finds any 6-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param tag    the tag for the blocks that can be accepted in the set
     */
    public static LongSet findSix(ServerWorld world, BlockPos origin, int depth, Tag<Block> tag) {
        return findSix(world, origin, depth, state -> state.isIn(tag));
    }

    /**
     * Finds any 6-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param block  the block type that can be accepted in the set
     */
    public static LongSet findSix(ServerWorld world, BlockPos origin, int depth, Block block) {
        return findSix(world, origin, depth, state -> state.isOf(block));
    }

    /**
     * Finds any 18-connected blocks and puts them in a {@link LongSet}..
     *
     * @param origin    the position of the first block
     * @param depth     the amount of maximum blocks to find
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static LongSet findEighteen(ServerWorld world, BlockPos origin, int depth, Predicate<BlockState> predicate) {
        LongSet set = new LongArraySet();
        Deque<BlockPos> ends = new ArrayDeque<>();
        ends.push(origin);
        BlockPos.Mutable local = origin.mutableCopy();
        while(depth > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.pollLast();
            for(Direction direction : Direction.values()) {
                local.set(pos).offset(direction);
                if(predicate.test(world.getBlockState(local)) && !set.contains(local.asLong()) && !ends.contains(local)) {
                    ends.push(local.toImmutable());
                }
            }
            for(int x = -1; x <= 1; x += 2) {
                for(int y = -1; y <= 1; y += 2) {
                    for(int z = -1; z <= 1; z += 2) {
                        local.set(pos).add(x, y, z);
                        BlockState state = world.getBlockState(local);
                        if(predicate.test(state) && !set.contains(local.asLong()) && !ends.contains(local)) {
                            ends.push(local.toImmutable());
                        }
                    }
                }
            }
            set.add(pos.asLong());
            depth--;
        }
        return set;
    }

    /**
     * Finds any 18-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param tag    the tag for the blocks that can be accepted in the set
     */
    public static LongSet findEighteen(ServerWorld world, BlockPos origin, int depth, Tag<Block> tag) {
        return findEighteen(world, origin, depth, state -> state.isIn(tag));
    }

    /**
     * Finds any 18-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param block  the block type that can be accepted in the set
     */
    public static LongSet findEighteen(ServerWorld world, BlockPos origin, int depth, Block block) {
        return findEighteen(world, origin, depth, state -> state.isOf(block));
    }

    /**
     * Finds any 26-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin    the position of the first block
     * @param depth     the amount of maximum blocks to find
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static LongSet findTwentySix(ServerWorld world, BlockPos origin, int depth, Predicate<BlockState> predicate) {
        LongSet set = new LongArraySet();
        Deque<BlockPos> ends = new ArrayDeque<>();
        ends.push(origin);
        BlockPos.Mutable local = origin.mutableCopy();
        while(depth > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.pollLast();
            for(int x = -1; x <= 1; x++) {
                for(int y = -1; y <= 1; y++) {
                    for(int z = -1; z <= 1; z++) {
                        if(x == 0 && y == 0 && z == 0) continue;
                        local.set(pos).add(x, y, z);
                        if(predicate.test(world.getBlockState(local)) && !set.contains(local.asLong()) && !ends.contains(local)) {
                            ends.push(local.toImmutable());
                        }
                    }
                }
            }
            set.add(pos.asLong());
            depth--;
        }
        return set;
    }

    /**
     * Finds any 26-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param tag    the tag for the blocks that can be accepted in the set
     */
    public static LongSet findTwentySix(ServerWorld world, BlockPos origin, int depth, Tag<Block> tag) {
        return findTwentySix(world, origin, depth, state -> state.isIn(tag));
    }

    /**
     * Finds any 26-connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param depth  the amount of maximum blocks to find
     * @param block  the block type that can be accepted in the set
     */
    public static LongSet findTwentySix(ServerWorld world, BlockPos origin, int depth, Block block) {
        return findTwentySix(world, origin, depth, state -> state.isOf(block));
    }
}