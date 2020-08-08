package net.gegy1000.plasmid.game.map.template.trace;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface RegionTracer {
    void startTracing(BlockPos origin);

    void trace(BlockPos pos);

    void finishTracing(BlockPos pos);

    boolean isTracing();

    @Nullable
    PartialRegion getTracing();

    @Nullable
    PartialRegion takeReady();

    void setMode(RegionTraceMode mode);

    RegionTraceMode getMode();
}
