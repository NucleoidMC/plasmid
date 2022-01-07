package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;

public final class ColoredBlocks {
    public static Block wool(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
            case WHITE -> Blocks.WHITE_WOOL;
        };
    }

    public static Block carpet(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_CARPET;
            case MAGENTA -> Blocks.MAGENTA_CARPET;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CARPET;
            case YELLOW -> Blocks.YELLOW_CARPET;
            case LIME -> Blocks.LIME_CARPET;
            case PINK -> Blocks.PINK_CARPET;
            case GRAY -> Blocks.GRAY_CARPET;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CARPET;
            case CYAN -> Blocks.CYAN_CARPET;
            case PURPLE -> Blocks.PURPLE_CARPET;
            case BLUE -> Blocks.BLUE_CARPET;
            case BROWN -> Blocks.BROWN_CARPET;
            case GREEN -> Blocks.GREEN_CARPET;
            case RED -> Blocks.RED_CARPET;
            case BLACK -> Blocks.BLACK_CARPET;
            case WHITE -> Blocks.WHITE_CARPET;
        };
    }

    public static Block terracotta(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_TERRACOTTA;
            case MAGENTA -> Blocks.MAGENTA_TERRACOTTA;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_TERRACOTTA;
            case YELLOW -> Blocks.YELLOW_TERRACOTTA;
            case LIME -> Blocks.LIME_TERRACOTTA;
            case PINK -> Blocks.PINK_TERRACOTTA;
            case GRAY -> Blocks.GRAY_TERRACOTTA;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_TERRACOTTA;
            case CYAN -> Blocks.CYAN_TERRACOTTA;
            case PURPLE -> Blocks.PURPLE_TERRACOTTA;
            case BLUE -> Blocks.BLUE_TERRACOTTA;
            case BROWN -> Blocks.BROWN_TERRACOTTA;
            case GREEN -> Blocks.GREEN_TERRACOTTA;
            case RED -> Blocks.RED_TERRACOTTA;
            case BLACK -> Blocks.BLACK_TERRACOTTA;
            case WHITE -> Blocks.WHITE_TERRACOTTA;
        };
    }

    public static Block glazedTerracotta(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_GLAZED_TERRACOTTA;
            case MAGENTA -> Blocks.MAGENTA_GLAZED_TERRACOTTA;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA;
            case YELLOW -> Blocks.YELLOW_GLAZED_TERRACOTTA;
            case LIME -> Blocks.LIME_GLAZED_TERRACOTTA;
            case PINK -> Blocks.PINK_GLAZED_TERRACOTTA;
            case GRAY -> Blocks.GRAY_GLAZED_TERRACOTTA;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA;
            case CYAN -> Blocks.CYAN_GLAZED_TERRACOTTA;
            case PURPLE -> Blocks.PURPLE_GLAZED_TERRACOTTA;
            case BLUE -> Blocks.BLUE_GLAZED_TERRACOTTA;
            case BROWN -> Blocks.BROWN_GLAZED_TERRACOTTA;
            case GREEN -> Blocks.GREEN_GLAZED_TERRACOTTA;
            case RED -> Blocks.RED_GLAZED_TERRACOTTA;
            case BLACK -> Blocks.BLACK_GLAZED_TERRACOTTA;
            case WHITE -> Blocks.WHITE_GLAZED_TERRACOTTA;
        };
    }

    public static Block concrete(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_CONCRETE;
            case MAGENTA -> Blocks.MAGENTA_CONCRETE;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CONCRETE;
            case YELLOW -> Blocks.YELLOW_CONCRETE;
            case LIME -> Blocks.LIME_CONCRETE;
            case PINK -> Blocks.PINK_CONCRETE;
            case GRAY -> Blocks.GRAY_CONCRETE;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CONCRETE;
            case CYAN -> Blocks.CYAN_CONCRETE;
            case PURPLE -> Blocks.PURPLE_CONCRETE;
            case BLUE -> Blocks.BLUE_CONCRETE;
            case BROWN -> Blocks.BROWN_CONCRETE;
            case GREEN -> Blocks.GREEN_CONCRETE;
            case RED -> Blocks.RED_CONCRETE;
            case BLACK -> Blocks.BLACK_CONCRETE;
            case WHITE -> Blocks.WHITE_CONCRETE;
        };
    }

    public static Block concretePowder(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_CONCRETE_POWDER;
            case MAGENTA -> Blocks.MAGENTA_CONCRETE_POWDER;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CONCRETE_POWDER;
            case YELLOW -> Blocks.YELLOW_CONCRETE_POWDER;
            case LIME -> Blocks.LIME_CONCRETE_POWDER;
            case PINK -> Blocks.PINK_CONCRETE_POWDER;
            case GRAY -> Blocks.GRAY_CONCRETE_POWDER;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CONCRETE_POWDER;
            case CYAN -> Blocks.CYAN_CONCRETE_POWDER;
            case PURPLE -> Blocks.PURPLE_CONCRETE_POWDER;
            case BLUE -> Blocks.BLUE_CONCRETE_POWDER;
            case BROWN -> Blocks.BROWN_CONCRETE_POWDER;
            case GREEN -> Blocks.GREEN_CONCRETE_POWDER;
            case RED -> Blocks.RED_CONCRETE_POWDER;
            case BLACK -> Blocks.BLACK_CONCRETE_POWDER;
            case WHITE -> Blocks.WHITE_CONCRETE_POWDER;
        };
    }

    public static Block glass(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS;
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS;
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS;
            case LIME -> Blocks.LIME_STAINED_GLASS;
            case PINK -> Blocks.PINK_STAINED_GLASS;
            case GRAY -> Blocks.GRAY_STAINED_GLASS;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS;
            case CYAN -> Blocks.CYAN_STAINED_GLASS;
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS;
            case BLUE -> Blocks.BLUE_STAINED_GLASS;
            case BROWN -> Blocks.BROWN_STAINED_GLASS;
            case GREEN -> Blocks.GREEN_STAINED_GLASS;
            case RED -> Blocks.RED_STAINED_GLASS;
            case BLACK -> Blocks.BLACK_STAINED_GLASS;
            case WHITE -> Blocks.WHITE_STAINED_GLASS;
        };
    }

    public static Block glassPane(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS_PANE;
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS_PANE;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS_PANE;
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS_PANE;
            case LIME -> Blocks.LIME_STAINED_GLASS_PANE;
            case PINK -> Blocks.PINK_STAINED_GLASS_PANE;
            case GRAY -> Blocks.GRAY_STAINED_GLASS_PANE;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS_PANE;
            case CYAN -> Blocks.CYAN_STAINED_GLASS_PANE;
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS_PANE;
            case BLUE -> Blocks.BLUE_STAINED_GLASS_PANE;
            case BROWN -> Blocks.BROWN_STAINED_GLASS_PANE;
            case GREEN -> Blocks.GREEN_STAINED_GLASS_PANE;
            case RED -> Blocks.RED_STAINED_GLASS_PANE;
            case BLACK -> Blocks.BLACK_STAINED_GLASS_PANE;
            case WHITE -> Blocks.WHITE_STAINED_GLASS_PANE;
        };
    }

    public static Block bed(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_BED;
            case MAGENTA -> Blocks.MAGENTA_BED;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_BED;
            case YELLOW -> Blocks.YELLOW_BED;
            case LIME -> Blocks.LIME_BED;
            case PINK -> Blocks.PINK_BED;
            case GRAY -> Blocks.GRAY_BED;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_BED;
            case CYAN -> Blocks.CYAN_BED;
            case PURPLE -> Blocks.PURPLE_BED;
            case BLUE -> Blocks.BLUE_BED;
            case BROWN -> Blocks.BROWN_BED;
            case GREEN -> Blocks.GREEN_BED;
            case RED -> Blocks.RED_BED;
            case BLACK -> Blocks.BLACK_BED;
            case WHITE -> Blocks.WHITE_BED;
        };
    }

    public static Block banner(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_BANNER;
            case MAGENTA -> Blocks.MAGENTA_BANNER;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_BANNER;
            case YELLOW -> Blocks.YELLOW_BANNER;
            case LIME -> Blocks.LIME_BANNER;
            case PINK -> Blocks.PINK_BANNER;
            case GRAY -> Blocks.GRAY_BANNER;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_BANNER;
            case CYAN -> Blocks.CYAN_BANNER;
            case PURPLE -> Blocks.PURPLE_BANNER;
            case BLUE -> Blocks.BLUE_BANNER;
            case BROWN -> Blocks.BROWN_BANNER;
            case GREEN -> Blocks.GREEN_BANNER;
            case RED -> Blocks.RED_BANNER;
            case BLACK -> Blocks.BLACK_BANNER;
            case WHITE -> Blocks.WHITE_BANNER;
        };
    }

    public static Block wallBanner(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_WALL_BANNER;
            case MAGENTA -> Blocks.MAGENTA_WALL_BANNER;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WALL_BANNER;
            case YELLOW -> Blocks.YELLOW_WALL_BANNER;
            case LIME -> Blocks.LIME_WALL_BANNER;
            case PINK -> Blocks.PINK_WALL_BANNER;
            case GRAY -> Blocks.GRAY_WALL_BANNER;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WALL_BANNER;
            case CYAN -> Blocks.CYAN_WALL_BANNER;
            case PURPLE -> Blocks.PURPLE_WALL_BANNER;
            case BLUE -> Blocks.BLUE_WALL_BANNER;
            case BROWN -> Blocks.BROWN_WALL_BANNER;
            case GREEN -> Blocks.GREEN_WALL_BANNER;
            case RED -> Blocks.RED_WALL_BANNER;
            case BLACK -> Blocks.BLACK_WALL_BANNER;
            case WHITE -> Blocks.WHITE_WALL_BANNER;
        };
    }

    public static Block shulkerBox(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW -> Blocks.YELLOW_SHULKER_BOX;
            case LIME -> Blocks.LIME_SHULKER_BOX;
            case PINK -> Blocks.PINK_SHULKER_BOX;
            case GRAY -> Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN -> Blocks.CYAN_SHULKER_BOX;
            case PURPLE -> Blocks.PURPLE_SHULKER_BOX;
            case BLUE -> Blocks.BLUE_SHULKER_BOX;
            case BROWN -> Blocks.BROWN_SHULKER_BOX;
            case GREEN -> Blocks.GREEN_SHULKER_BOX;
            case RED -> Blocks.RED_SHULKER_BOX;
            case BLACK -> Blocks.BLACK_SHULKER_BOX;
            case WHITE -> Blocks.WHITE_SHULKER_BOX;
        };
    }

    public static Block candle(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_CANDLE;
            case MAGENTA -> Blocks.MAGENTA_CANDLE;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CANDLE;
            case YELLOW -> Blocks.YELLOW_CANDLE;
            case LIME -> Blocks.LIME_CANDLE;
            case PINK -> Blocks.PINK_CANDLE;
            case GRAY -> Blocks.GRAY_CANDLE;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CANDLE;
            case CYAN -> Blocks.CYAN_CANDLE;
            case PURPLE -> Blocks.PURPLE_CANDLE;
            case BLUE -> Blocks.BLUE_CANDLE;
            case BROWN -> Blocks.BROWN_CANDLE;
            case GREEN -> Blocks.GREEN_CANDLE;
            case RED -> Blocks.RED_CANDLE;
            case BLACK -> Blocks.BLACK_CANDLE;
            case WHITE -> Blocks.WHITE_CANDLE;
        };
    }

    public static Block candleCake(DyeColor color) {
        return switch (color) {
            case ORANGE -> Blocks.ORANGE_CANDLE_CAKE;
            case MAGENTA -> Blocks.MAGENTA_CANDLE_CAKE;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CANDLE_CAKE;
            case YELLOW -> Blocks.YELLOW_CANDLE_CAKE;
            case LIME -> Blocks.LIME_CANDLE_CAKE;
            case PINK -> Blocks.PINK_CANDLE_CAKE;
            case GRAY -> Blocks.GRAY_CANDLE_CAKE;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CANDLE_CAKE;
            case CYAN -> Blocks.CYAN_CANDLE_CAKE;
            case PURPLE -> Blocks.PURPLE_CANDLE_CAKE;
            case BLUE -> Blocks.BLUE_CANDLE_CAKE;
            case BROWN -> Blocks.BROWN_CANDLE_CAKE;
            case GREEN -> Blocks.GREEN_CANDLE_CAKE;
            case RED -> Blocks.RED_CANDLE_CAKE;
            case BLACK -> Blocks.BLACK_CANDLE_CAKE;
            case WHITE -> Blocks.WHITE_CANDLE_CAKE;
        };
    }
}
