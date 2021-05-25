package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Provides functionality for traversing the graph of the 3D block grid.
 * <p>
 * This is particularly useful for implementing algorithms such as flood-fill.
 */
public final class BlockTraversal {
    private Connectivity connectivity = Connectivity.SIX;
    private Order order = Order.BREADTH_FIRST;

    private BlockTraversal() {
    }

    /**
     * Creates a {@link BlockTraversal} instance with default settings.
     * This implies six-connectivity and breadth-first traversal order.
     *
     * @return a new {@link BlockTraversal} instance
     */
    public static BlockTraversal create() {
        return new BlockTraversal();
    }

    /**
     * Sets the type of {@link Connectivity} to use for block traversal.
     * Is set to {@link Connectivity#SIX} by default.
     *
     * @return this {@link BlockTraversal} instance for chaining
     * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
     */
    public BlockTraversal connectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
        return this;
    }

    /**
     * Sets the {@link Order} to use for block traversal.
     * Is set to {@link Order#BREADTH_FIRST} by default.
     *
     * @return this {@link BlockTraversal} instance for chaining
     * @see Order
     * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first search</a>
     * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search</a>
     */
    public BlockTraversal order(Order order) {
        this.order = order;
        return this;
    }

    /**
     * Traverses the block graph from the given origin by calling the given {@link Visitor}.
     * <p>
     * The given visitor should control how traversal exits. If it does not return {@link Result#TERMINATE}, blocks
     * will be traversed infinitely!
     *
     * @param origin the origin block position to start traversing from
     * @param visitor the visitor to call with each traversed position
     */
    public void accept(BlockPos origin, Visitor visitor) {
        State state = new State(this.order);

        state.tryVisit(origin);
        if (visitor.visit(origin, null, 0) == Result.TERMINATE) {
            return;
        }

        state.enqueue(origin, 0);

        BlockPos.Mutable nextPos = new BlockPos.Mutable();
        BlockPos.Mutable fromPos = new BlockPos.Mutable();
        long[] offsets = this.connectivity.offsets;

        while (!state.isComplete()) {
            fromPos.set(state.dequeuePos());
            int fromDepth = state.dequeueDepth();
            int nextDepth = fromDepth + 1;

            for (long offset : offsets) {
                nextPos.set(fromPos,
                        BlockPos.unpackLongX(offset),
                        BlockPos.unpackLongY(offset),
                        BlockPos.unpackLongZ(offset)
                );

                if (state.tryVisit(nextPos) && visitor.visit(nextPos, fromPos, nextDepth) == Result.CONTINUE) {
                    state.enqueue(nextPos, nextDepth);
                }
            }
        }
    }

    static final class State {
        private final Order order;

        private final LongSet visited = new LongOpenHashSet();
        private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        private final ShortArrayFIFOQueue depthQueue = new ShortArrayFIFOQueue();

        State(Order order) {
            this.order = order;
        }

        boolean tryVisit(BlockPos pos) {
            return this.visited.add(pos.asLong());
        }

        void enqueue(BlockPos pos, int depth) {
            this.queue.enqueue(pos.asLong());
            this.depthQueue.enqueue((short) depth);
        }

        long dequeuePos() {
            return this.order == Order.BREADTH_FIRST ? this.queue.dequeueLastLong() : this.queue.dequeueLong();
        }

        int dequeueDepth() {
            return this.order == Order.BREADTH_FIRST ? this.depthQueue.dequeueLastShort() : this.depthQueue.dequeueShort();
        }

        boolean isComplete() {
            return this.queue.isEmpty();
        }
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Pixel_connectivity#3-dimensional">3-dimensional pixel connectivity</a>
     */
    public static final class Connectivity {
        public static final Connectivity SIX = create(Connectivity::six);
        public static final Connectivity EIGHTEEN = create(Connectivity::eighteen);
        public static final Connectivity TWENTY_SIX = create(Connectivity::twentySix);

        final long[] offsets;

        Connectivity(long[] offsets) {
            this.offsets = offsets;
        }

        static Connectivity create(Consumer<Consumer<Vec3i>> generator) {
            LongList offsets = new LongArrayList();
            generator.accept(pos -> {
                long packed = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
                offsets.add(packed);
            });
            return new Connectivity(offsets.toLongArray());
        }

        private static void six(Consumer<Vec3i> consumer) {
            for (Direction direction : Direction.values()) {
                consumer.accept(direction.getVector());
            }
        }

        private static void eighteen(Consumer<Vec3i> consumer) {
            six(consumer);

            for (int x = -1; x <= 1; x += 2) {
                for (int y = -1; y <= 1; y += 2) {
                    consumer.accept(new BlockPos(x, y, 0));
                    consumer.accept(new BlockPos(0, x, y));
                    consumer.accept(new BlockPos(y, 0, x));
                }
            }
        }

        private static void twentySix(Consumer<Vec3i> consumer) {
            eighteen(consumer);

            for (int z = -1; z <= 1; z += 2) {
                for (int x = -1; x <= 1; x += 2) {
                    for (int y = -1; y <= 1; y += 2) {
                        consumer.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Graph_traversal">Graph Traversal</a>
     */
    public enum Order {
        /**
         * Breadth-first search traverses all neighbors at the current depth before proceeding to the next depth level.
         *
         * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first search</a>
         */
        BREADTH_FIRST,
        /**
         * Depth-first search traverses each branch as far as possible before trying other branches.
         *
         * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search</a>
         */
        DEPTH_FIRST
    }

    /**
     * Accepts each block traversed by a {@link BlockTraversal} instance and controls how the algorithm should advance.
     */
    public interface Visitor {
        /**
         * Called for each traversed block and determines whether traversal should continue from each block.
         *
         * @param pos the current block position
         * @param fromPos the block position the current was found from. Can be {@code null} for the origin!
         * @param depth the depth at the current block (how many steps we have taken from the origin)
         * @return whether or not traversal should continue from this point
         */
        Result visit(BlockPos pos, @Nullable BlockPos fromPos, int depth);
    }

    public enum Result {
        /**
         * Continue traversing from this block and consider all connected edges.
         */
        CONTINUE,
        /**
         * Stop traversing from this block and ignore any connected edges.
         * <p>
         * This will not stop traversal altogether, it just signals that this branch should terminate.
         */
        TERMINATE
    }
}
