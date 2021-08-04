package xyz.nucleoid.plasmid.util;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

public final class Guis {
    private Guis() {}

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, GuiElementInterface... elements) {
        var gui = new SimpleGui(selectScreenType(elements.length), player, false);
        gui.setTitle(text);

        buildSelector(gui, elements);
        return gui;
    }

    public static Layer createSelectorLayer(int height, int width, Collection<GuiElementInterface> elements) {
        return createSelectorLayer(height, width, elements.toArray(new GuiElementInterface[0]));
    }

    public static Layer createSelectorLayer(int height, int width, GuiElementInterface... elements) {
        var gui = new Layer(height, width);
        buildSelector(gui, elements);
        return gui;
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, elements.toArray(new GuiElementInterface[0]));
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
}
