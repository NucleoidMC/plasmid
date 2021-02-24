package xyz.nucleoid.plasmid.map.workspace.trace;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public enum RegionTraceMode {
    OFFSET(new TranslatableText("item.plasmid.add_region.trace_mode.offset")),
    EXACT(new TranslatableText("item.plasmid.add_region.trace_mode.exact")),
    AT_FEET(new TranslatableText("item.plasmid.add_region.trace_mode.at_feet"));

    private final Text name;

    RegionTraceMode(Text name) {
        this.name = name;
    }

    @Nullable
    public BlockPos tryTrace(PlayerEntity player) {
        if (this == AT_FEET) {
            return player.getBlockPos();
        }

        HitResult traceResult = player.raycast(64.0, 1.0F, true);
        if (traceResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) traceResult;
            BlockPos pos = blockResult.getBlockPos();

            if (this == OFFSET) {
                pos = pos.offset(blockResult.getSide());
            }

            return pos;
        }

        return null;
    }

    public RegionTraceMode next() {
        RegionTraceMode[] modes = values();
        return modes[(this.ordinal() + 1) % modes.length];
    }

    public Text getName() {
        return this.name;
    }
}
