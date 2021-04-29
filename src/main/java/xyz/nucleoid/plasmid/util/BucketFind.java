package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class BucketFind {
    /**
     * Finds any near blocks and puts them in a set.
     *
     * @param pos      The position of the first block
     * @param blockTag The tag of the blocks that can be accepted in the set
     */
    public static Set<BlockPos> find(ServerWorld world, BlockPos pos, Tag<Block> blockTag) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(pos);
        searchAround(world, pos, positions, blockTag);
        return positions;
    }

    private static void searchAround(ServerWorld world, BlockPos pos, Set<BlockPos> positions, Tag<Block> blockTag) {
        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                for(int y = -1; y <= 1; y++) {
                    BlockPos local = pos.add(x, y, z);
                    BlockState state = world.getBlockState(local);

                    if(!positions.contains(local)) {
                        if(state.isIn(blockTag)) {
                            positions.add(local);
                            searchAround(world, local, positions, blockTag);
                        }
                    }
                }
            }
        }
    }
}
