package xyz.nucleoid.plasmid.api.util;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.function.Consumer;

public final class Guis {
    private Guis() {
    }

    public static SimpleGui createSelectorGui(ServerPlayer player, MutableComponent text, boolean includePlayerSlots, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, GuiElement... elements) {
        var gui = new SimpleGui(selectScreenType(elements.length), player, includePlayerSlots) {
            @Override
            public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
                onClick.accept(this);
                return super.onClick(index, type, action, element);
            }

            @Override
            public void onManualClose() {
                onClose.accept(this);
            }
        };

        gui.setTitle(text);

        buildSelector(gui, elements);
        return gui;
    }

    public static SimpleGui createSelectorGui(ServerPlayer player, MutableComponent text, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, GuiElement... elements) {
        return createSelectorGui(player, text, false, onClick, onClose, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayer player, MutableComponent text, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, Collection<GuiElement> elements) {
        return createSelectorGui(player, text, false, onClick, onClose, elements);
    }

    public static SimpleGui createSelectorGui(ServerPlayer player, MutableComponent text, boolean includePlayerSlots, Consumer<SimpleGui> onClick, Consumer<SimpleGui> onClose, Collection<GuiElement> elements) {
        return createSelectorGui(player, text, includePlayerSlots, onClick, onClose, elements.toArray(new GuiElement[0]));
    }

    public static SimpleGui createSelectorGui(ServerPlayer player, MutableComponent text, boolean includePlayerSlots, Collection<GuiElement> elements) {
        return createSelectorGui(player, text, includePlayerSlots, gui -> {}, gui -> {}, elements.toArray(new GuiElement[0]));
    }

    public static Layer createSelectorLayer(int height, int width, Collection<GuiElement> elements) {
        return createSelectorLayer(height, width, elements.toArray(new GuiElement[0]));
    }

    public static Layer createSelectorLayer(int height, int width, GuiElement... elements) {
        var gui = new Layer(height, width);
        buildSelector(gui, elements);
        return gui;
    }

    private static void buildSelector(SlotHolder holder, GuiElement... elements) {
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

    public static ItemStack getNumericBanner(HolderGetter.Provider registries, @Range(from = 0, to = 9) int value) {
        HolderGetter<BannerPattern> patterns = registries.lookupOrThrow(Registries.BANNER_PATTERN);
        return switch (Math.abs(value) % 10) {
            case 0 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY)
            );
            case 1 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_CENTER, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.SQUARE_TOP_LEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.CURLY_BORDER, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 2 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_DOWNLEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 3 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.CURLY_BORDER, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 4 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.HALF_HORIZONTAL_MIRROR, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 5 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_DOWNRIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 6 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.HALF_HORIZONTAL, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 7 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_DOWNLEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 8 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_DOWNRIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            case 9 -> createBanner(new BannerPatternLayers.Builder()
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_LEFT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.HALF_HORIZONTAL_MIRROR, DyeColor.GRAY)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_RIGHT, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.WHITE)
                    .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.GRAY));
            default -> throw new IllegalStateException();
        };
    }

    private static MenuType<?> selectScreenType(int rowCount) {
        return switch (Mth.ceil(((float) rowCount) / 9)) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    private static ItemStack createBanner(BannerPatternLayers.Builder patterns) {
        ItemStack stack = Items.BANNER.gray().getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, CommonComponents.EMPTY);
        stack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, tooltipDisplay -> tooltipDisplay.withHidden(DataComponents.BANNER_PATTERNS, true));
        stack.set(DataComponents.BANNER_PATTERNS, patterns.build());
        return stack;
    }
}
