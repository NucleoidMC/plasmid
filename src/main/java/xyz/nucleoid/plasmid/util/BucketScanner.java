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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Methods for finding connected blocks as a {@link LongSet}. Use {@link BlockPos#fromLong} to retrieve returned positions.
 */
public final class BucketScanner {
    private final Builder builder;

    private final Map<Long, Integer> depthMap;

    private final Deque<Long> scanQueue;
    private final BlockPos.Mutable previousMutable;
    private final BlockPos.Mutable nextMutable;

    private BucketScanner(Builder builder, BlockPos origin) {
        this.builder = builder;
        this.depthMap = new HashMap<>();

        this.scanQueue = new ArrayDeque<>();
        this.previousMutable = origin.mutableCopy();
        this.nextMutable = origin.mutableCopy();

        scanQueue.push(origin.asLong());
        depthMap.put(origin.asLong(), builder.maxDepth);

        this.initialize();
    }

    /**
     * Creates a bucket scanner builder.
     *
     * @param predicate the predicate for the blocks that can be accepted in the set
     * @return a new bucket scanner builder
     */
    public static Builder create(ScanPredicate predicate) {
        return new Builder(predicate, Connectivity.SIX, SearchType.DEPTH_FIRST, 512, -1);
    }

    /**
     * Creates a bucket scanner builder.
     *
     * @param block the block type that can be accepted in the set
     * @return a new bucket scanner builder
     */
    public static Builder create(Block block, ServerWorld world) {
        return create((previousPos, nextPos) -> world.getBlockState(nextPos).isOf(block));
    }

    /**
     * Creates a bucket scanner builder.
     *
     * @param tag the tag for the blocks that can be accepted in the set
     * @return a new bucket scanner builder
     */
    public static Builder create(Tag<Block> tag, ServerWorld world) {
        return create((previousPos, pos) -> world.getBlockState(pos).isIn(tag));
    }

    /**
     * Creates a bucket scanner builder.
     *
     * @param ruleTest the rule test for the blocks that can be accepted in the set
     * @return a new bucket scanner builder
     */
    public static Builder create(RuleTest ruleTest, ServerWorld world, Random random) {
        return create((previousPos, nextPos) -> ruleTest.test(world.getBlockState(nextPos), random));
    }

    private void initialize() {
        int amount = 1;
        while(amount != builder.maxAmount) {
            if(scanQueue.isEmpty()) {
                return;
            }
            long pos;
            switch(builder.searchType) {
                case BREADTH_FIRST:
                default:
                    pos = scanQueue.pollLast();
                    break;
                case DEPTH_FIRST:
                    pos = scanQueue.pollFirst();
                    break;
            }
            if(builder.connectivity == Connectivity.TWENTY_SIX) {
                for(int i = -1; i <= 1; i += 2) {
                    for(int j = -1; j <= 1; j += 2) {
                        for(int k = -1; k <= 1; k += 2) {
                            scan(pos, i, j, k);
                        }
                    }
                }
            }
            if(builder.connectivity != Connectivity.SIX) {
                for(int i = -1; i <= 1; i += 2) {
                    for(int j = -1; j <= 1; j += 2) {
                        scan(pos, i, j, 0);
                        scan(pos, 0, i, j);
                        scan(pos, j, 0, i);
                    }
                }
            }
            for(Direction direction : Direction.values()) {
                scan(pos, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
            }
            amount++;
        }
    }

    private void scan(long previousLong, int offsetX, int offsetY, int offsetZ) {
        this.previousMutable.set(previousLong);
        this.nextMutable.set(previousLong).move(offsetX, offsetY, offsetZ);
        if(test(previousMutable, nextMutable)) {
            long nextLong = nextMutable.asLong();
            int nextDepth = depthMap.get(previousLong) - 1;
            if(nextDepth > 0) {
                scanQueue.push(nextLong);
            }
            depthMap.put(nextLong, nextDepth);
        }
    }

    private boolean test(BlockPos previousPos, BlockPos nextPos) {
        return this.builder.predicate.test(previousPos, nextPos) && !depthMap.containsKey(nextPos.asLong()) && !scanQueue.contains(nextPos.asLong());
    }

    /**
     * @return the set of positions found by the scanner
     */
    public LongSet getPositions() {
        return new LongArraySet(depthMap.keySet());
    }

    /**
     * Gives a set of positions depending on the depth values of those positions.
     *
     * @param predicate determines if a position should enter the set depending of its depth value
     * @return the set of positions found by the scanner that verify the predicate
     */
    public LongSet getPositions(Predicate<Integer> predicate) {
        return new LongArraySet(depthMap.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).mapToLong(Map.Entry::getKey).boxed().collect(Collectors.toSet()));
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

    @FunctionalInterface
    public interface ScanPredicate {
        /**
         * Method executed at each branch end to fuel the scanning process and spread around.
         *
         * @param previousPos the position of a branch end
         * @param nextPos     the position of the block being scanned next to the branch end
         */
        boolean test(BlockPos previousPos, BlockPos nextPos);
    }

    public static class Builder {
        private final ScanPredicate predicate;
        private int maxAmount;
        private int maxDepth;
        private Connectivity connectivity;
        private SearchType searchType;

        private Builder(ScanPredicate predicate, Connectivity connectivity, SearchType searchType, int maxDepth, int maxAmount) {
            this.maxAmount = maxAmount;
            this.maxDepth = maxDepth;
            this.connectivity = connectivity;
            this.searchType = searchType;
            this.predicate = predicate;
        }

        /**
         * The maximum amount of blocks that can be scanned.
         * <p>Set to -1 to disable this.
         * <p>Is set to -1 by default.
         *
         * @return this builder for chaining
         */
        public Builder maxAmount(int max) {
            this.maxAmount = max;
            return this;
        }

        /**
         * The maximum length of branches that can be scanned.
         * <p>Cannot be set to a value under 0.
         * <p>Is set to 512 by default.
         *
         * @return this builder for chaining
         */
        public Builder maxDepth(int max) {
            this.maxDepth = Math.max(max, 0);
            return this;
        }

        /**
         * The type of connectivity to use to search blocks around a branch end.
         * Is set to {@link Connectivity#SIX} by default.
         *
         * @return this builder for chaining
         * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
         */
        public Builder connectivity(Connectivity c) {
            this.connectivity = c;
            return this;
        }

        /**
         * Is set to {@link SearchType#DEPTH_FIRST} by default.
         *
         * @return this builder for chaining
         * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first search</a>
         * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search</a>
         */
        public Builder searchType(SearchType s) {
            this.searchType = s;
            return this;
        }

        /**
         * Executes the scanning process.
         *
         * @param origin the origin position of the scan
         * @return a new {@link BucketScanner}
         */
        public BucketScanner build(BlockPos origin) {
            return new BucketScanner(this, origin);
        }
    }
}