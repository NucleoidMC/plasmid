package xyz.nucleoid.plasmid.game.map.template.trace;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class PartialRegion {
    private final BlockPos origin;
    private BlockPos target;

    public PartialRegion(BlockPos origin) {
        this.origin = origin;
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public BlockPos getMin() {
        if (this.target == null) {
            return this.origin;
        }
        return BlockBounds.min(this.origin, this.target);
    }

    public BlockPos getMax() {
        if (this.target == null) {
            return this.origin;
        }
        return BlockBounds.max(this.origin, this.target);
    }
}
