package xyz.nucleoid.plasmid.duck;

import it.unimi.dsi.fastutil.longs.LongSet;

public interface ServerEntityManagerAccess {
    void plasmid$clearChunks(LongSet chunksToDrop);
}
