package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Methods for finding connected blocks as a {@link LongSet}. Use {@link BlockPos#fromLong} to retrieve returned positions.
 */
public final class BucketScanner {
    private final LongSet set;
    private final Deque<Long> ends;
    private final Map<Long, Integer> endDepthMap;

    private final Builder builder;
    private final BlockPos.Mutable mutable;

    private BucketScanner(Builder builder, BlockPos origin) {
        this.set = new LongArraySet();
        this.ends = new ArrayDeque<>();
        this.endDepthMap = new HashMap<>();

        this.builder = builder;
        this.mutable = origin.mutableCopy();

        this.ends.push(origin.asLong());
        this.endDepthMap.put(origin.asLong(), builder.maxDepth);

        this.scan();
    }

    /**
     * @param predicate the predicate for the blocks that can be accepted in the set
     */
    public static Builder create(BiPredicate<BlockPos, BlockPos> predicate) {
        return new Builder(predicate, Connectivity.SIX, SearchType.DEPTH_FIRST, 512, -1);
    }

    /**
     * @param block the block type that can be accepted in the set
     */
    public static Builder create(Block block, ServerWorld world) {
        return create((previousPos, pos) -> world.getBlockState(pos).isOf(block));
    }

    /**
     * @param tag the tag for the blocks that can be accepted in the set
     */
    public static Builder create(Tag<Block> tag, ServerWorld world) {
        return create((previousPos, pos) -> world.getBlockState(pos).isIn(tag));
    }

    /**
     * @param ruleTest the rule test for the blocks that can be accepted in the set
     */
    public static Builder create(RuleTest ruleTest, ServerWorld world, Random random) {
        return create((previousPos, pos) -> ruleTest.test(world.getBlockState(pos), random));
    }

    private void scan() {
        int amount = 1;
        while (amount != this.builder.maxAmount) {
            if (this.ends.isEmpty()) {
                return;
            }
            long pos;
            switch (this.builder.searchType) {
                case BREADTH_FIRST:
                default:
                    pos = this.ends.pollLast();
                    break;
                case DEPTH_FIRST:
                    pos = this.ends.pollFirst();
                    break;
            }
            if (this.builder.connectivity == Connectivity.TWENTY_SIX) {
                for (int i = -1; i <= 1; i += 2) {
                    for (int j = -1; j <= 1; j += 2) {
                        for (int k = -1; k <= 1; k += 2) {
                            this.scan(pos, i, j, k);
                        }
                    }
                }
            }
            if (this.builder.connectivity != Connectivity.SIX) {
                for (int i = -1; i <= 1; i += 2) {
                    for (int j = -1; j <= 1; j += 2) {
                        this.scan(pos, i, j, 0);
                        this.scan(pos, 0, i, j);
                        this.scan(pos, j, 0, i);
                    }
                }
            }
            for (Direction direction : Direction.values()) {
                this.scan(pos, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
            }
            this.set.add(pos);
            amount++;
        }
    }

    private void scan(long previousLong, int offsetX, int offsetY, int offsetZ) {
        BlockPos previousPos = BlockPos.fromLong(previousLong);
        this.mutable.set(previousPos.getX() + offsetX, previousPos.getY() + offsetY, previousPos.getZ() + offsetZ);
        if (this.test(previousPos, this.mutable, this.set, this.ends)) {
            long offsetLong = this.mutable.asLong();
            int depth = this.endDepthMap.get(previousLong) - 1;
            if (depth > 0) {
                this.ends.push(offsetLong);
                this.endDepthMap.put(offsetLong, depth);
            } else {
                this.set.add(offsetLong);
            }
        }
    }

    private boolean test(BlockPos previousPos, BlockPos offsetPos, LongSet set, Deque<Long> ends) {
        return this.builder.predicate.test(previousPos, offsetPos) && !set.contains(offsetPos.asLong()) && !ends.contains(offsetPos.asLong());
    }

    public LongSet getPositions() {
        return this.set;
    }

    public enum Connectivity {
        SIX,
        EIGHTEEN,
        TWENTY_SIX
    }

    public enum SearchType {
        BREADTH_FIRST,
        DEPTH_FIRST
    }

    public static class Builder {
        private final BiPredicate<BlockPos, BlockPos> predicate;
        private int maxAmount;
        private int maxDepth;
        private Connectivity connectivity;
        private SearchType searchType;

        private Builder(BiPredicate<BlockPos, BlockPos> predicate, Connectivity connectivity, SearchType searchType, int maxDepth, int maxAmount) {
            this.maxAmount = maxAmount;
            this.maxDepth = maxDepth;
            this.connectivity = connectivity;
            this.searchType = searchType;
            this.predicate = predicate;
        }

        /**
         * The maximum amount of blocks that can be scanned.
         */
        public Builder maxAmount(int max) {
            this.maxAmount = max;
            return this;
        }

        /**
         * The maximum length of branches that can be scanned.
         */
        public Builder maxDepth(int max) {
            this.maxDepth = max;
            return this;
        }

        /**
         * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
         */
        public Builder connectivity(Connectivity c) {
            this.connectivity = c;
            return this;
        }

        /**
         * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first search</a>
         * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search</a>
         */
        public Builder searchType(SearchType s) {
            this.searchType = s;
            return this;
        }

        public BucketScanner build(BlockPos origin) {
            return new BucketScanner(this, origin);
        }
    }
}