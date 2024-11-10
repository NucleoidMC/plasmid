package xyz.nucleoid.plasmid.util;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Unit;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.function.Consumer;

public final class Guis {
    private Guis() {
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, boolean includePlayerSlots, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, GuiElementInterface... elements) {
        var gui = new SimpleGui(selectScreenType(elements.length), player, includePlayerSlots) {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                onClick.accept(this);
                return super.onClick(index, type, action, element);
            }

            @Override
            public void onClose() {
                onClose.accept(this);
            }
        };

        gui.setTitle(text);

        buildSelector(gui, elements);
        return gui;
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, GuiElementInterface... elements) {
        return createSelectorGui(player, text, false, onClick, onClose, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, false, onClick, onClose, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, boolean includePlayerSlots, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, includePlayerSlots, onClick, onClose, elements.toArray(new GuiElementInterface[0]));
    }

    public static SimpleGui createSelectorGui(ServerPlayerEntity player, MutableText text, boolean includePlayerSlots, Collection<GuiElementInterface> elements) {
        return createSelectorGui(player, text, includePlayerSlots, gui -> {}, gui -> {}, elements.toArray(new GuiElementInterface[0]));
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

    public static ItemStack getNumericBanner(RegistryEntryLookup.RegistryLookup registries, @Range(from = 0, to = 9) int value) {
        RegistryEntryLookup<BannerPattern> patterns = registries.getOrThrow(RegistryKeys.BANNER_PATTERN);
        return switch (Math.abs(value) % 10) {
            case 0 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY)
            );
            case 1 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_CENTER, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.SQUARE_TOP_LEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.CURLY_BORDER, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 2 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.RHOMBUS, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_DOWNLEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 3 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.CURLY_BORDER, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 4 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.HALF_HORIZONTAL_BOTTOM, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 5 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.RHOMBUS, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_DOWNRIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 6 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.HALF_HORIZONTAL, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 7 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_DOWNLEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 8 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_DOWNRIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 9 -> createBanner(new BannerPatternsComponent.Builder()
                    .add(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.HALF_HORIZONTAL_BOTTOM, DyeColor.GRAY)
                    .add(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .add(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            default -> throw new IllegalStateException();
        };
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

    private static ItemStack createBanner(BannerPatternsComponent.Builder patterns) {
        ItemStack stack = Items.GRAY_BANNER.getDefaultStack();
        stack.set(DataComponentTypes.CUSTOM_NAME, ScreenTexts.EMPTY);
        stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        stack.set(DataComponentTypes.BANNER_PATTERNS, patterns.build());
        return stack;
    }
}
