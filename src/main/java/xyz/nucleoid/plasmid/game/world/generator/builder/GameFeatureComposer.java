package xyz.nucleoid.plasmid.game.world.generator.builder;

import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.StructureAccessor;

public interface GameFeatureComposer {
    static GameFeatureComposer none() {
        return (region, structures) -> {};
    }

    void addFeatures(ChunkRegion region, StructureAccessor structures);
}
