package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Methods for finding connected blocks as a {@link LongSet}. Use {@link BlockPos#fromLong} to retrieve returned positions.
 */
public final class BucketScanner {
    private BucketScanner() {
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin   the position of the first block
     * @param amount   the amount of maximum blocks to find
     * @param ruleTest the rule test for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, Type depth, Connectivity connectivity, int amount, RuleTest ruleTest, ServerWorld world, Random random) {
        return find(origin, depth, connectivity, amount, pos -> ruleTest.test(world.getBlockState(pos), random));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param amount the amount of maximum blocks to find
     * @param tag    the tag for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, Type depth, Connectivity connectivity, int amount, Tag<Block> tag, ServerWorld world) {
        return find(origin, depth, connectivity, amount, pos -> world.getBlockState(pos).isIn(tag));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin the position of the first block
     * @param amount the amount of maximum blocks to find
     * @param block  the block type that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, Type depth, Connectivity connectivity, int amount, Block block, ServerWorld world) {
        return find(origin, depth, connectivity, amount, pos -> world.getBlockState(pos).isOf(block));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin    the position of the first block
     * @param amount    the amount of maximum blocks to find
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, Type depth, Connectivity connectivity, int amount, Predicate<BlockPos> predicate) {
        LongSet set = new LongArraySet();
        Deque<BlockPos> ends = new ArrayDeque<>();
        ends.push(origin);
        BlockPos.Mutable mutable = origin.mutableCopy();
        while(amount > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos;
            switch(depth) {
                case BREADTH:
                default:
                    pos = ends.pollLast();
                    break;
                case DEPTH:
                    pos = ends.pollFirst();
                    break;
            }
            if(connectivity == Connectivity.TWENTY_SIX) {
                for(int i = -1; i <= 1; i += 2) {
                    for(int j = -1; j <= 1; j += 2) {
                        for(int k = -1; k <= 1; k += 2) {
                            scan(mutable.set(pos.add(i, j, k)), set, ends, predicate);
                        }
                    }
                }
            }
            if(connectivity != Connectivity.SIX) {
                for(int i = -1; i <= 1; i += 2) {
                    for(int j = -1; j <= 1; j += 2) {
                        scan(mutable.set(pos.add(i, j, 0)), set, ends, predicate);
                        scan(mutable.set(pos.add(0, i, j)), set, ends, predicate);
                        scan(mutable.set(pos.add(j, 0, i)), set, ends, predicate);
                    }
                }
            }
            for(Direction direction : Direction.values()) {
                scan(mutable.set(pos.offset(direction)), set, ends, predicate);
            }
            set.add(pos.asLong());
            amount--;
        }
        return set;
    }

    private static void scan(BlockPos.Mutable mutable, LongSet set, Deque<BlockPos> ends, Predicate<BlockPos> predicate) {
        if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
            ends.push(mutable.toImmutable());
        }
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
     */
    public enum Connectivity {
        SIX,
        EIGHTEEN,
        TWENTY_SIX
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first search</a>
     * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search</a>
     */
    public enum Type {
        BREADTH,
        DEPTH
    }
}