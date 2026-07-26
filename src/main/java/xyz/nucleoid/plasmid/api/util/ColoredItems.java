package xyz.nucleoid.plasmid.api.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@Deprecated
public final class ColoredItems {
    public static Item dye(DyeColor color) {
        return Items.DYE.pick(color);
    }

    public static Item bundle(DyeColor color) {
        return Items.DYED_BUNDLE.pick(color);
    }

    public static Item harness(DyeColor color) {
        return Items.HARNESS.pick(color);
    }
}
