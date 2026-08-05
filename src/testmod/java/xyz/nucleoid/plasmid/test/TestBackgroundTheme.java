package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.menu.*;

/**
 * A theme whose background is a resource pack texture, the way Nucleoid Extras does it.
 * <p>
 * The background is not a slot: it is a glyph in a custom font, injected into the title. Two space
 * characters either side pull the cursor to the container's left edge and back, so the image lands over the
 * whole window and the title still prints where it should. Players without the pack get the plain title, so
 * the menu stays usable.
 * <p>
 * It also takes over every tooltip through {@link #entryElement}, rebuilding names and lore rather than
 * decorating the defaults.
 */
public record TestBackgroundTheme() implements GameMenuTheme {
    public static final MapCodec<TestBackgroundTheme> CODEC = MapCodec.unit(TestBackgroundTheme::new);

    private static final Identifier FONT = Identifier.fromNamespaceAndPath(TestInitializer.ID, "menu");
    private static final Style FONT_STYLE = Style.EMPTY.withColor(0xFFFFFF).withFont(new FontDescription.Resource(FONT));

    /**
     * Pull the cursor left to the window edge, and back to where the title belongs.
     */
    private static final char PULL_TO_EDGE = '\uE000';
    private static final char PULL_BACK = '\uE002';

    /**
     * One background glyph per container height: U+E011 draws a 1-row window, U+E016 a 6-row one.
     */
    private static final char FIRST_BACKGROUND = '\uE011';

    private static final int ACCENT = 0xD65DD6;
    private static final int MUTED = 0x9A6BA8;

    /**
     * Sprites at {@code textures/gui/sprites/tooltip/panel_{background,frame}.png}.
     */
    private static final Identifier TOOLTIP = Identifier.fromNamespaceAndPath(TestInitializer.ID, "panel");

    private static final ItemStackTemplate BACK_ICON = icon("back");
    private static final ItemStackTemplate FILTER_ICON = icon("filter");
    private static final ItemStackTemplate FILTER_ACTIVE_ICON = icon("filter_active");

    /**
     * Any item works: {@code ITEM_MODEL} replaces what is rendered.
     * <p>
     * Points at a hand-written item asset in {@code assets/testmod/items/} rather than a bridged one, so the
     * icons resolve from whatever pack carries the assets instead of only from a generated pack.
     */
    private static ItemStackTemplate icon(String name) {
        var model = Identifier.fromNamespaceAndPath(TestInitializer.ID, "menu_" + name);

        return new ItemStackTemplate(Items.MUSIC_DISC_5, DataComponentPatch.builder()
                .set(DataComponents.ITEM_MODEL, model)
                .build());
    }

    /**
     * A navigation row below the content and nothing else; the artwork covers the rest.
     */
    private static final GameMenuInsets INSETS = new GameMenuInsets(0, 1, 0, 0, 1, 0);

    @Override
    public GameMenuInsets baseInsets() {
        return INSETS;
    }

    @Override
    public Component title(GameMenuContext ctx, Component title) {
        var styled = title.copy().withStyle(style -> style.withColor(ACCENT));

        if (!PolymerResourcePackUtils.hasMainPack(ctx.player())) {
            return styled;
        }

        return Component.empty()
                .append(Component.literal(background(ctx.frame().containerRows())).setStyle(FONT_STYLE))
                .append(styled);
    }

    /**
     * Pull to the window edge, draw the background cut to this many rows, pull back.
     */
    private static String background(int containerRows) {
        int rows = Mth.clamp(containerRows, 1, GameMenuFrame.MAX_ROWS);
        return "" + PULL_TO_EDGE + (char) (FIRST_BACKGROUND + rows - 1) + PULL_BACK;
    }

    /**
     * Shared by every element the theme draws, so the whole menu shares one tooltip look.
     */
    private static GuiElementBuilder themed(ItemStack stack) {
        return GuiElementBuilder.from(stack)
                .hideDefaultTooltip()
                .setComponent(DataComponents.TOOLTIP_STYLE, TOOLTIP);
    }

    @Override
    public GuiElement entryElement(GameMenuContext ctx, GameMenuEntry entry) {
        var element = themed(entry.icon().copy())
                .setItemName(Component.literal("❯ ").withColor(MUTED)
                        .append(entry.name().copy().withStyle(style -> style.withColor(ACCENT).withBold(true))));

        for (var line : entry.description()) {
            element.addLoreLine(Component.literal("  ").append(line.copy().withStyle(style -> style.withColor(MUTED).withItalic(false))));
        }

        int players = entry.getPlayerCount();
        if (players > -1) {
            int max = entry.getMaxPlayerCount();
            element.addLoreLine(Component.literal("  " + players + (max > 0 ? " / " + max : "") + " playing")
                    .withStyle(style -> style.withColor(ACCENT).withItalic(false)));
        }

        var state = entry.getState();
        if (state != null) {
            element.addLoreLine(Component.literal("  ").append(state.copy().withStyle(style -> style.withColor(MUTED).withItalic(false))));
        }

        if (entry.getAction() != GameMenuEntry.Action.NONE) {
            element.addLoreLine(Component.literal("  ▸ ").withColor(MUTED)
                    .append(entry.getAction().text().copy().withStyle(style -> style.withColor(ACCENT).withItalic(false))));
        }

        // Hybrid entries carry a second destination here; a theme drawing its own lore has to ask for it.
        if (entry.getAltAction() != GameMenuEntry.Action.NONE) {
            element.addLoreLine(Component.literal("  ▸ ").withColor(MUTED)
                    .append(entry.getAltAction().textAlt().copy().withStyle(style -> style.withColor(ACCENT).withItalic(false))));
        }

        // The entry owns its click behaviour; rebuilding it here would drop right-click actions.
        return element.setCallback(entry.createGuiElement().getGuiCallback()).build();
    }

    @Override
    public GuiElementBuilder chrome(GameMenuContext ctx, GameMenuFeature feature) {
        boolean active = ctx.isActive(feature);
        var appearance = feature.appearance(active);

        // Every control is replaceable: swap the stack and the theme owns the artwork outright.
        ItemStack stack;
        if (feature == GameMenuFeatures.BACK) {
            stack = BACK_ICON.create();
        } else if (feature == GameMenuFeatures.GAME_FILTER) {
            stack = (active ? FILTER_ACTIVE_ICON : FILTER_ICON).create();
        } else {
            stack = appearance.icon().create();
        }

        return themed(stack)
                .setItemName(Component.literal("❯ ").withColor(MUTED)
                        .append(appearance.name().copy().withStyle(style -> style.withColor(ACCENT).withBold(true))));
    }

    @Override
    public GuiElementBuilder emptyElement(GameMenuContext ctx) {
        return themed(Items.STAINED_GLASS_PANE.lightGray().getDefaultInstance())
                .setItemName(Component.literal("Nothing to show").withStyle(style -> style.withColor(MUTED)));
    }

    @Override
    public void decorate(GameMenuContext ctx, Layer chrome) {
        if (PolymerResourcePackUtils.hasMainPack(ctx.player())) {
            // The texture already draws the bar, at whatever height this menu came out.
            return;
        }

        var filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.magenta()).hideTooltip().build();
        int navRow = ctx.frame().y() + ctx.frame().height();

        for (int x = 0; x < chrome.getWidth() && navRow < chrome.getHeight(); x++) {
            chrome.setSlot(navRow * chrome.getWidth() + x, filler);
        }
    }

    @Override
    public MapCodec<? extends GameMenuTheme> codec() {
        return CODEC;
    }
}
