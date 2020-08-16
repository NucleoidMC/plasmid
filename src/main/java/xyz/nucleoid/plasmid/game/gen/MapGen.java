package xyz.nucleoid.plasmid.game.gen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;

import java.util.Random;

public interface MapGen {
    void generate(ServerWorldAccess world, BlockPos pos, Random random);
}
