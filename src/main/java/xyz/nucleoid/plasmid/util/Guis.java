package xyz.nucleoid.plasmid.util;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public final class Guis {
    private static final ItemStack[] NUMBERS = new ItemStack[] {
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:ls,Color:0},{Pattern:ts,Color:0},{Pattern:rs,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:cs,Color:0},{Pattern:tl,Color:0},{Pattern:cbo,Color:7},{Pattern:bs,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:ts,Color:0},{Pattern:mr,Color:7},{Pattern:bs,Color:0},{Pattern:dls,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:cbo,Color:7},{Pattern:rs,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:ls,Color:0},{Pattern:hhb,Color:7},{Pattern:rs,Color:0},{Pattern:ms,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:mr,Color:7},{Pattern:ts,Color:0},{Pattern:drs,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:bs,Color:0},{Pattern:rs,Color:0},{Pattern:hh,Color:7},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:ls,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:dls,Color:0},{Pattern:ts,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:dls,Color:0},{Pattern:ts,Color:0},{Pattern:bo,Color:7}]}}"),
            createBanner("{BlockEntityTag:{Patterns:[{Pattern:ls,Color:0},{Pattern:hhb,Color:7},{Pattern:ms,Color:0},{Pattern:ts,Color:0},{Pattern:rs,Color:0},{Pattern:bs,Color:0},{Pattern:bo,Color:7}]}}")
    };

    private Guis() {
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, boolean includePlayerSlots, GuiElementInterface... elements) {
        var gui = new SimpleGui(selectScreenType(elements.length), player, includePlayerSlots);
        gui.setTitle(text);

        buildSelector(gui, elements);
        return gui;
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, GuiElementInterface... elements) {
        return createSelectorGui(player, text, false, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, false, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, boolean includePlayerSlots, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, includePlayerSlots, elements.toArray(new GuiElementInterface[0]));
    }

    public static Layer createSelectorLayer(int height, int width, Collection<GuiElementInterface> elements) {
        return createSelectorLayer(height, width, elements.toArray(new GuiElementInterface[0]));
    }

    public static Layer createSelectorLayer(int height, int width, GuiElementInterface... elements) {
        var gui = new Layer(height, width);
        buildSelector(gui, elements);
        return gui;
    }

    private static void buildSelector(SlotHolder holder, GuiElementInterface... elements) {
        int lastRowCount = elements.length % holder.getWidth();
        int skippedElements = elements.length - lastRowCount;

        for (int i = 0; i < skippedElements; i++) {
            holder.setSlot(i, elements[i]);
        }

        int offset = (holder.getWidth() - lastRowCount) / 2;

        for (int i = skippedElements; i < elements.length; i++) {
            holder.setSlot(i + offset, elements[i]);
        }
    }

    public static ItemStack getNumericBanner(@Range(from = 0, to = 9) int value) {
        return NUMBERS[Math.abs(value) % 10];
    }

    private static ScreenHandlerType<?> selectScreenType(int rowCount) {
        return switch (MathHelper.ceil(((float) rowCount) / 9)) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }

    private static ItemStack createBanner(String nbt) {
        ItemStack stack = Items.GRAY_BANNER.getDefaultStack();
        try {
            stack.setNbt(StringNbtReader.parse(nbt));
            stack.setCustomName(LiteralText.EMPTY);
            stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        } catch (Exception e) {}

        return stack;
    }
}
