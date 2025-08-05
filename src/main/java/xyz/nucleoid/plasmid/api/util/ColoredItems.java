package xyz.nucleoid.plasmid.api.util;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

public final class ColoredItems {
    public static Item dye(DyeColor color) {
        return switch (color) {
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
            case WHITE -> Items.WHITE_DYE;
        };
    }

    public static Item bundle(DyeColor color) {
        return switch (color) {
            case ORANGE -> Items.ORANGE_BUNDLE;
            case MAGENTA -> Items.MAGENTA_BUNDLE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_BUNDLE;
            case YELLOW -> Items.YELLOW_BUNDLE;
            case LIME -> Items.LIME_BUNDLE;
            case PINK -> Items.PINK_BUNDLE;
            case GRAY -> Items.GRAY_BUNDLE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_BUNDLE;
            case CYAN -> Items.CYAN_BUNDLE;
            case PURPLE -> Items.PURPLE_BUNDLE;
            case BLUE -> Items.BLUE_BUNDLE;
            case BROWN -> Items.BROWN_BUNDLE;
            case GREEN -> Items.GREEN_BUNDLE;
            case RED -> Items.RED_BUNDLE;
            case BLACK -> Items.BLACK_BUNDLE;
            case WHITE -> Items.WHITE_BUNDLE;
        };
    }

    public static Item harness(DyeColor color) {
        return switch (color) {
            case ORANGE -> Items.ORANGE_HARNESS;
            case MAGENTA -> Items.MAGENTA_HARNESS;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_HARNESS;
            case YELLOW -> Items.YELLOW_HARNESS;
            case LIME -> Items.LIME_HARNESS;
            case PINK -> Items.PINK_HARNESS;
            case GRAY -> Items.GRAY_HARNESS;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_HARNESS;
            case CYAN -> Items.CYAN_HARNESS;
            case PURPLE -> Items.PURPLE_HARNESS;
            case BLUE -> Items.BLUE_HARNESS;
            case BROWN -> Items.BROWN_HARNESS;
            case GREEN -> Items.GREEN_HARNESS;
            case RED -> Items.RED_HARNESS;
            case BLACK -> Items.BLACK_HARNESS;
            case WHITE -> Items.WHITE_HARNESS;
        };
    }
}
