package xyz.nucleoid.plasmid.map.workspace.trace;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

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
