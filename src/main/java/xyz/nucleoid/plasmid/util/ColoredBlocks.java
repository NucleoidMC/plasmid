package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;

public final class ColoredBlocks {
    public static Block wool(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_WOOL;
            case MAGENTA: return Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_WOOL;
            case YELLOW: return Blocks.YELLOW_WOOL;
            case LIME: return Blocks.LIME_WOOL;
            case PINK: return Blocks.PINK_WOOL;
            case GRAY: return Blocks.GRAY_WOOL;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_WOOL;
            case CYAN: return Blocks.CYAN_WOOL;
            case PURPLE: return Blocks.PURPLE_WOOL;
            case BLUE: return Blocks.BLUE_WOOL;
            case BROWN: return Blocks.BROWN_WOOL;
            case GREEN: return Blocks.GREEN_WOOL;
            case RED: return Blocks.RED_WOOL;
            case BLACK: return Blocks.BLACK_WOOL;
            default:
            case WHITE: return Blocks.WHITE_WOOL;
        }
    }

    public static Block carpet(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_CARPET;
            case MAGENTA: return Blocks.MAGENTA_CARPET;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_CARPET;
            case YELLOW: return Blocks.YELLOW_CARPET;
            case LIME: return Blocks.LIME_CARPET;
            case PINK: return Blocks.PINK_CARPET;
            case GRAY: return Blocks.GRAY_CARPET;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_CARPET;
            case CYAN: return Blocks.CYAN_CARPET;
            case PURPLE: return Blocks.PURPLE_CARPET;
            case BLUE: return Blocks.BLUE_CARPET;
            case BROWN: return Blocks.BROWN_CARPET;
            case GREEN: return Blocks.GREEN_CARPET;
            case RED: return Blocks.RED_CARPET;
            case BLACK: return Blocks.BLACK_CARPET;
            default:
            case WHITE: return Blocks.WHITE_CARPET;
        }
    }

    public static Block terracotta(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_TERRACOTTA;
            case MAGENTA: return Blocks.MAGENTA_TERRACOTTA;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_TERRACOTTA;
            case YELLOW: return Blocks.YELLOW_TERRACOTTA;
            case LIME: return Blocks.LIME_TERRACOTTA;
            case PINK: return Blocks.PINK_TERRACOTTA;
            case GRAY: return Blocks.GRAY_TERRACOTTA;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_TERRACOTTA;
            case CYAN: return Blocks.CYAN_TERRACOTTA;
            case PURPLE: return Blocks.PURPLE_TERRACOTTA;
            case BLUE: return Blocks.BLUE_TERRACOTTA;
            case BROWN: return Blocks.BROWN_TERRACOTTA;
            case GREEN: return Blocks.GREEN_TERRACOTTA;
            case RED: return Blocks.RED_TERRACOTTA;
            case BLACK: return Blocks.BLACK_TERRACOTTA;
            case WHITE: return Blocks.WHITE_TERRACOTTA;
            default: return Blocks.TERRACOTTA;
        }
    }

    public static Block glazedTerracotta(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_GLAZED_TERRACOTTA;
            case MAGENTA: return Blocks.MAGENTA_GLAZED_TERRACOTTA;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA;
            case YELLOW: return Blocks.YELLOW_GLAZED_TERRACOTTA;
            case LIME: return Blocks.LIME_GLAZED_TERRACOTTA;
            case PINK: return Blocks.PINK_GLAZED_TERRACOTTA;
            case GRAY: return Blocks.GRAY_GLAZED_TERRACOTTA;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA;
            case CYAN: return Blocks.CYAN_GLAZED_TERRACOTTA;
            case PURPLE: return Blocks.PURPLE_GLAZED_TERRACOTTA;
            case BLUE: return Blocks.BLUE_GLAZED_TERRACOTTA;
            case BROWN: return Blocks.BROWN_GLAZED_TERRACOTTA;
            case GREEN: return Blocks.GREEN_GLAZED_TERRACOTTA;
            case RED: return Blocks.RED_GLAZED_TERRACOTTA;
            case BLACK: return Blocks.BLACK_GLAZED_TERRACOTTA;
            default:
            case WHITE: return Blocks.WHITE_GLAZED_TERRACOTTA;
        }
    }

    public static Block concrete(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_CONCRETE;
            case MAGENTA: return Blocks.MAGENTA_CONCRETE;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_CONCRETE;
            case YELLOW: return Blocks.YELLOW_CONCRETE;
            case LIME: return Blocks.LIME_CONCRETE;
            case PINK: return Blocks.PINK_CONCRETE;
            case GRAY: return Blocks.GRAY_CONCRETE;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_CONCRETE;
            case CYAN: return Blocks.CYAN_CONCRETE;
            case PURPLE: return Blocks.PURPLE_CONCRETE;
            case BLUE: return Blocks.BLUE_CONCRETE;
            case BROWN: return Blocks.BROWN_CONCRETE;
            case GREEN: return Blocks.GREEN_CONCRETE;
            case RED: return Blocks.RED_CONCRETE;
            case BLACK: return Blocks.BLACK_CONCRETE;
            default:
            case WHITE: return Blocks.WHITE_CONCRETE;
        }
    }

    public static Block concretePowder(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_CONCRETE_POWDER;
            case MAGENTA: return Blocks.MAGENTA_CONCRETE_POWDER;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_CONCRETE_POWDER;
            case YELLOW: return Blocks.YELLOW_CONCRETE_POWDER;
            case LIME: return Blocks.LIME_CONCRETE_POWDER;
            case PINK: return Blocks.PINK_CONCRETE_POWDER;
            case GRAY: return Blocks.GRAY_CONCRETE_POWDER;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_CONCRETE_POWDER;
            case CYAN: return Blocks.CYAN_CONCRETE_POWDER;
            case PURPLE: return Blocks.PURPLE_CONCRETE_POWDER;
            case BLUE: return Blocks.BLUE_CONCRETE_POWDER;
            case BROWN: return Blocks.BROWN_CONCRETE_POWDER;
            case GREEN: return Blocks.GREEN_CONCRETE_POWDER;
            case RED: return Blocks.RED_CONCRETE_POWDER;
            case BLACK: return Blocks.BLACK_CONCRETE_POWDER;
            default:
            case WHITE: return Blocks.WHITE_CONCRETE_POWDER;
        }
    }

    public static Block glass(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_STAINED_GLASS;
            case MAGENTA: return Blocks.MAGENTA_STAINED_GLASS;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_STAINED_GLASS;
            case YELLOW: return Blocks.YELLOW_STAINED_GLASS;
            case LIME: return Blocks.LIME_STAINED_GLASS;
            case PINK: return Blocks.PINK_STAINED_GLASS;
            case GRAY: return Blocks.GRAY_STAINED_GLASS;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_STAINED_GLASS;
            case CYAN: return Blocks.CYAN_STAINED_GLASS;
            case PURPLE: return Blocks.PURPLE_STAINED_GLASS;
            case BLUE: return Blocks.BLUE_STAINED_GLASS;
            case BROWN: return Blocks.BROWN_STAINED_GLASS;
            case GREEN: return Blocks.GREEN_STAINED_GLASS;
            case RED: return Blocks.RED_STAINED_GLASS;
            case BLACK: return Blocks.BLACK_STAINED_GLASS;
            case WHITE: return Blocks.WHITE_STAINED_GLASS;
            default: return Blocks.GLASS;
        }
    }

    public static Block glassPane(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_STAINED_GLASS_PANE;
            case MAGENTA: return Blocks.MAGENTA_STAINED_GLASS_PANE;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_STAINED_GLASS_PANE;
            case YELLOW: return Blocks.YELLOW_STAINED_GLASS_PANE;
            case LIME: return Blocks.LIME_STAINED_GLASS_PANE;
            case PINK: return Blocks.PINK_STAINED_GLASS_PANE;
            case GRAY: return Blocks.GRAY_STAINED_GLASS_PANE;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_STAINED_GLASS_PANE;
            case CYAN: return Blocks.CYAN_STAINED_GLASS_PANE;
            case PURPLE: return Blocks.PURPLE_STAINED_GLASS_PANE;
            case BLUE: return Blocks.BLUE_STAINED_GLASS_PANE;
            case BROWN: return Blocks.BROWN_STAINED_GLASS_PANE;
            case GREEN: return Blocks.GREEN_STAINED_GLASS_PANE;
            case RED: return Blocks.RED_STAINED_GLASS_PANE;
            case BLACK: return Blocks.BLACK_STAINED_GLASS_PANE;
            case WHITE: return Blocks.WHITE_STAINED_GLASS_PANE;
            default: return Blocks.GLASS_PANE;
        }
    }

    public static Block bed(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_BED;
            case MAGENTA: return Blocks.MAGENTA_BED;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_BED;
            case YELLOW: return Blocks.YELLOW_BED;
            case LIME: return Blocks.LIME_BED;
            case PINK: return Blocks.PINK_BED;
            case GRAY: return Blocks.GRAY_BED;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_BED;
            case CYAN: return Blocks.CYAN_BED;
            case PURPLE: return Blocks.PURPLE_BED;
            case BLUE: return Blocks.BLUE_BED;
            case BROWN: return Blocks.BROWN_BED;
            case GREEN: return Blocks.GREEN_BED;
            case RED: return Blocks.RED_BED;
            case BLACK: return Blocks.BLACK_BED;
            default:
            case WHITE: return Blocks.WHITE_BED;
        }
    }

    public static Block banner(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_BANNER;
            case MAGENTA: return Blocks.MAGENTA_BANNER;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_BANNER;
            case YELLOW: return Blocks.YELLOW_BANNER;
            case LIME: return Blocks.LIME_BANNER;
            case PINK: return Blocks.PINK_BANNER;
            case GRAY: return Blocks.GRAY_BANNER;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_BANNER;
            case CYAN: return Blocks.CYAN_BANNER;
            case PURPLE: return Blocks.PURPLE_BANNER;
            case BLUE: return Blocks.BLUE_BANNER;
            case BROWN: return Blocks.BROWN_BANNER;
            case GREEN: return Blocks.GREEN_BANNER;
            case RED: return Blocks.RED_BANNER;
            case BLACK: return Blocks.BLACK_BANNER;
            default:
            case WHITE: return Blocks.WHITE_BANNER;
        }
    }

    public static Block wallBanner(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_WALL_BANNER;
            case MAGENTA: return Blocks.MAGENTA_WALL_BANNER;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_WALL_BANNER;
            case YELLOW: return Blocks.YELLOW_WALL_BANNER;
            case LIME: return Blocks.LIME_WALL_BANNER;
            case PINK: return Blocks.PINK_WALL_BANNER;
            case GRAY: return Blocks.GRAY_WALL_BANNER;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_WALL_BANNER;
            case CYAN: return Blocks.CYAN_WALL_BANNER;
            case PURPLE: return Blocks.PURPLE_WALL_BANNER;
            case BLUE: return Blocks.BLUE_WALL_BANNER;
            case BROWN: return Blocks.BROWN_WALL_BANNER;
            case GREEN: return Blocks.GREEN_WALL_BANNER;
            case RED: return Blocks.RED_WALL_BANNER;
            case BLACK: return Blocks.BLACK_WALL_BANNER;
            default:
            case WHITE: return Blocks.WHITE_WALL_BANNER;
        }
    }

    public static Block shulkerBox(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA: return Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW: return Blocks.YELLOW_SHULKER_BOX;
            case LIME: return Blocks.LIME_SHULKER_BOX;
            case PINK: return Blocks.PINK_SHULKER_BOX;
            case GRAY: return Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN: return Blocks.CYAN_SHULKER_BOX;
            case PURPLE: return Blocks.PURPLE_SHULKER_BOX;
            case BLUE: return Blocks.BLUE_SHULKER_BOX;
            case BROWN: return Blocks.BROWN_SHULKER_BOX;
            case GREEN: return Blocks.GREEN_SHULKER_BOX;
            case RED: return Blocks.RED_SHULKER_BOX;
            case BLACK: return Blocks.BLACK_SHULKER_BOX;
            case WHITE: return Blocks.WHITE_SHULKER_BOX;
            default: return Blocks.SHULKER_BOX;
        }
    }
}
