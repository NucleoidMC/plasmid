package xyz.nucleoid.plasmid.duck;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface ThreadedAnvilChunkStorageAccess {
    void plasmid$setGenerator(ChunkGenerator generator);
    void plasmid$clearChunks(LongSet chunksToDrop);
}
