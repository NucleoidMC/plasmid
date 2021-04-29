package xyz.nucleoid.plasmid.util;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class BucketFind {
    /**
     * Finds any directly connected blocks and puts them in a set.
     *
     * @param origin The position of the first block
     * @param predicate The predicate for the blocks that can be accepted in the set
     * @param limit The amount of maximum blocks to find
     */
    public static Set<BlockPos> find(ServerWorld world, BlockPos origin, Predicate<BlockState> predicate, int limit) {
        Set<BlockPos> set = new HashSet<>();
        Set<BlockPos> ends = new HashSet<>();
        ends.add(origin);
        while(limit > 0) {
            if(ends.isEmpty()) {
                return set;
            }
            BlockPos pos = ends.stream().findAny().get();
            for(Direction direction : Direction.values()) {
                BlockPos.Mutable local = pos.offset(direction).mutableCopy();
                BlockState state = world.getBlockState(local);
                if(predicate.test(state)) {
                    if(!set.contains(local)) {
                        ends.add(local);
                    }
                }
            }
            set.add(pos);
            ends.remove(pos);
            limit--;
        }
        return set;
    }
}