package xyz.nucleoid.plasmid.test;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.plasmid.api.util.BlockTraversal;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BlockTraversalTests {
    @Test
    public void testSixConnectivity() {
        var expected = Set.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, 0, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.SIX);
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testEighteenConnectivity() {
        var expected = Set.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, 0, 1),

            new BlockPos(-1, -1, 0),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, -1, -1),
            new BlockPos(1, -1, 0),
            new BlockPos(1, 0, -1),
            new BlockPos(0, 1, -1),
            new BlockPos(-1, 1, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, -1, 1),
            new BlockPos(1, 1, 0),
            new BlockPos(1, 0, 1),
            new BlockPos(0, 1, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.EIGHTEEN);
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testTwentySixConnectivity() {
        var expected = Set.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, 0, 1),

            new BlockPos(-1, -1, 0),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, -1, -1),
            new BlockPos(1, -1, 0),
            new BlockPos(1, 0, -1),
            new BlockPos(0, 1, -1),
            new BlockPos(-1, 1, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, -1, 1),
            new BlockPos(1, 1, 0),
            new BlockPos(1, 0, 1),
            new BlockPos(0, 1, 1),

            new BlockPos(-1, -1, -1),
            new BlockPos(-1, -1, 1),
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, 1, 1),
            new BlockPos(1, -1, -1),
            new BlockPos(1, -1, 1),
            new BlockPos(1, 1, -1),
            new BlockPos(1, 1, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.TWENTY_SIX);
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testFourConnectivityAxisX() {
        var expected = Set.of(
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.four(Direction.Axis.X));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testFourConnectivityAxisY() {
        var expected = Set.of(
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.four(Direction.Axis.Y));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testFourConnectivityAxisZ() {
        var expected = Set.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.four(Direction.Axis.Z));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testEightConnectivityAxisX() {
        var expected = Set.of(
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1),

            new BlockPos(0, -1, -1),
            new BlockPos(0, -1, 1),
            new BlockPos(0, 1, -1),
            new BlockPos(0, 1, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.eight(Direction.Axis.X));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testEightConnectivityAxisY() {
        var expected = Set.of(
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),

            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.eight(Direction.Axis.Y));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    @Test
    public void testEightConnectivityAxisZ() {
        var expected = Set.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0),

            new BlockPos(-1, -1, 0),
            new BlockPos(-1, 1, 0),
            new BlockPos(1, -1, 0),
            new BlockPos(1, 1, 0)
        );

        var traversal = BlockTraversal.create().connectivity(BlockTraversal.Connectivity.eight(Direction.Axis.Z));
        assertSingleTraversalVisits(expected, BlockPos.ORIGIN, traversal);
    }

    private void assertSingleTraversalVisits(Set<BlockPos> expected, BlockPos origin, BlockTraversal traversal) {
        Set<BlockPos> actual = new HashSet<>();

        traversal.accept(origin, (pos, fromPos, depth) -> {
            if (depth >= 1) {
                actual.add(pos.toImmutable());
                return BlockTraversal.Result.TERMINATE;
            }

            return BlockTraversal.Result.CONTINUE;
        });

        assertEquals(expected, actual);
    }
}
