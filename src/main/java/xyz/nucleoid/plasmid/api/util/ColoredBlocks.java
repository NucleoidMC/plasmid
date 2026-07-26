package xyz.nucleoid.plasmid.api.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;


@Deprecated
public final class ColoredBlocks {
    public static Block wool(DyeColor color) {
        return Blocks.WOOL.pick(color);
    }

    public static Block carpet(DyeColor color) {
        return Blocks.CARPET.pick(color);
    }

    public static Block terracotta(DyeColor color) {
        return Blocks.DYED_TERRACOTTA.pick(color);
    }

    public static Block glazedTerracotta(DyeColor color) {
        return Blocks.GLAZED_TERRACOTTA.pick(color);
    }

    public static Block concrete(DyeColor color) {
        return Blocks.CONCRETE.pick(color);
    }

    public static Block concretePowder(DyeColor color) {
        return Blocks.CONCRETE_POWDER.pick(color);
    }

    public static Block glass(DyeColor color) {
        return Blocks.STAINED_GLASS.pick(color);
    }

    public static Block glassPane(DyeColor color) {
        return Blocks.STAINED_GLASS_PANE.pick(color);
    }

    public static Block bed(DyeColor color) {
        return Blocks.BED.pick(color);
    }

    public static Block banner(DyeColor color) {
        return Blocks.BANNER.pick(color);
    }

    public static Block wallBanner(DyeColor color) {
        return Blocks.WALL_BANNER.pick(color);
    }

    public static Block shulkerBox(DyeColor color) {
        return Blocks.DYED_SHULKER_BOX.pick(color);
    }

    public static Block candle(DyeColor color) {
        return Blocks.DYED_CANDLE.pick(color);
    }

    public static Block candleCake(DyeColor color) {
        return Blocks.DYED_CANDLE_CAKE.pick(color);
    }
}
