package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
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
     * @param origin       the position of the first block
     * @param amount       the amount of maximum blocks to find
     * @param connectivity the type of scan that should be used to find blocks around a branch end
     * @param ruleTest     the rule test for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, int amount, Connectivity connectivity, RuleTest ruleTest, ServerWorld world, Random random) {
        return find(origin, amount, connectivity, pos -> ruleTest.test(world.getBlockState(pos), random));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin       the position of the first block
     * @param amount       the amount of maximum blocks to find
     * @param connectivity the type of scan that should be used to find blocks around a branch end
     * @param tag          the tag for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, int amount, Connectivity connectivity, Tag<Block> tag, ServerWorld world) {
        return find(origin, amount, connectivity, pos -> world.getBlockState(pos).isIn(tag));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin       the position of the first block
     * @param amount       the amount of maximum blocks to find
     * @param connectivity the type of scan that should be used to find blocks around a branch end
     * @param block        the block type that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, int amount, Connectivity connectivity, Block block, ServerWorld world) {
        return find(origin, amount, connectivity, pos -> world.getBlockState(pos).isOf(block));
    }

    /**
     * Finds any connected blocks and puts them in a {@link LongSet}.
     *
     * @param origin       the position of the first block
     * @param amount       the amount of maximum blocks to find
     * @param connectivity the type of scan that should be used to find blocks around a branch end
     * @param predicate    the predicate for the blocks that can be accepted in the set
     */
    public static LongSet find(BlockPos origin, int amount, Connectivity connectivity, Predicate<BlockPos> predicate) {
        LongSet set = new LongArraySet();
        Deque<BlockPos> ends = new ArrayDeque<>();
        ends.push(origin);
        BlockPos.Mutable mutable = origin.mutableCopy();
        while(amount > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.pollLast();
            switch(connectivity) {
                case EIGHTEEN:
                    for(int i = -1; i <= 1; i += 2) {
                        for(int j = -1; j <= 1; j += 2) {
                            mutable.set(pos.add(i, j, 0));
                            if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
                                ends.push(mutable.toImmutable());
                            }
                            mutable.set(pos.add(0, i, j));
                            if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
                                ends.push(mutable.toImmutable());
                            }
                            mutable.set(pos.add(j, 0, i));
                            if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
                                ends.push(mutable.toImmutable());
                            }
                        }
                    }
                case SIX:
                    for(Direction direction : Direction.values()) {
                        mutable.set(pos.offset(direction));
                        if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
                            ends.push(mutable.toImmutable());
                        }
                    }
                    break;
                case TWENTY_SIX:
                    for(int x = -1; x <= 1; x++) {
                        for(int y = -1; y <= 1; y++) {
                            for(int z = -1; z <= 1; z++) {
                                if(x == 0 && y == 0 && z == 0) continue;
                                mutable.set(pos.add(x, y, z));
                                if(predicate.test(mutable) && !set.contains(mutable.asLong()) && !ends.contains(mutable)) {
                                    ends.push(mutable.toImmutable());
                                }
                            }
                        }
                    }
                    break;
            }
            set.add(pos.asLong());
            amount--;
        }
        return set;
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
     */
    public enum Connectivity {
        SIX,
        EIGHTEEN,
        TWENTY_SIX
    }
}