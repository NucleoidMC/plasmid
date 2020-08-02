package net.gegy1000.plasmid.game.map.trace;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public enum RegionTraceMode {
    OFFSET(new LiteralText("offset mode")),
    EXACT(new LiteralText("exact mode")),
    AT_FEET(new LiteralText("at feet mode"));

    private final Text name;

    RegionTraceMode(Text name) {
        this.name = name;
    }

    @Nullable
    public BlockPos tryTrace(PlayerEntity player) {
        if (this == AT_FEET) {
            return player.getBlockPos();
        }

        HitResult traceResult = player.rayTrace(64.0, 1.0F, true);
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
