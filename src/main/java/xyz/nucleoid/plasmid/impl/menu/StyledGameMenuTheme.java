package xyz.nucleoid.plasmid.impl.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.Map;
import java.util.Optional;

/**
 * {@code plasmid:styled}: everything a theme can do from JSON, without code.
 *
 * <p>The plain {@code plasmid:simple} theme dresses a menu in filler and leaves the rest alone. This one adds
 * resource pack artwork behind the window, its own text styling for entries and controls, its own icons for
 * individual features, and its own empty-view element. A server with a pack should not need a mod to get the
 * look it wants.
 *
 * @param background artwork behind the window, which needs a resource pack to show
 * @param icons replaces what a feature's control is drawn with, keyed by feature id
 * @param fallback drawn instead of this whole theme for players without the pack. Without one, such a
 * player gets this theme minus its artwork, with the filler showing through instead
 */
public record StyledGameMenuTheme(
        GameMenuInsets insets,
        Optional<ItemStackTemplate> filler,
        Optional<GameMenuFeatureLayout> features,
        Optional<GameMenuBackground> background,
        GameMenuTextStyle title,
        GameMenuElementStyle entry,
        GameMenuElementStyle chrome,
        Map<GameMenuFeature, Icon> icons,
        Optional<Empty> empty,
        Optional<Holder<GameMenuTheme>> fallback
) implements GameMenuTheme {
    public static final MapCodec<StyledGameMenuTheme> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuInsets.CODEC.forGetter(StyledGameMenuTheme::insets),
            ItemStackTemplate.CODEC.optionalFieldOf("filler").forGetter(StyledGameMenuTheme::filler),
            GameMenuFeatureLayout.CODEC.optionalFieldOf("features").forGetter(StyledGameMenuTheme::features),
            GameMenuBackground.CODEC.optionalFieldOf("background").forGetter(StyledGameMenuTheme::background),
            GameMenuTextStyle.CODEC.optionalFieldOf("title", GameMenuTextStyle.NONE).forGetter(StyledGameMenuTheme::title),
            GameMenuElementStyle.CODEC.optionalFieldOf("entry", GameMenuElementStyle.NONE).forGetter(StyledGameMenuTheme::entry),
            GameMenuElementStyle.CODEC.optionalFieldOf("chrome", GameMenuElementStyle.NONE).forGetter(StyledGameMenuTheme::chrome),
            Codec.unboundedMap(GameMenuFeature.CODEC, Icon.CODEC).optionalFieldOf("icons", Map.of()).forGetter(StyledGameMenuTheme::icons),
            Empty.CODEC.optionalFieldOf("empty").forGetter(StyledGameMenuTheme::empty),
            GameMenuTheme.ENTRY_CODEC.optionalFieldOf("fallback").forGetter(StyledGameMenuTheme::fallback)
    ).apply(i, StyledGameMenuTheme::new));

    public StyledGameMenuTheme {
        icons = Map.copyOf(icons);
    }

    /**
     * Hands over to the fallback for players without the pack, so they get a whole coherent look rather than
     * this one with its artwork missing.
     */
    @Override
    public GameMenuTheme forPlayer(ServerPlayer player) {
        if (this.fallback.isPresent() && !PolymerResourcePackUtils.hasMainPack(player)) {
            return this.fallback.get().value();
        }

        return this;
    }

    /**
     * Room enough for both looks, so a menu too big for either is reported at load.
     *
     * <p>Validation only. What this draws with is {@link #insets(ServerPlayer)}, which must stay this
     * theme's own: a player who kept this theme is not the one the fallback's chrome was measured for.
     */
    @Override
    public GameMenuInsets baseInsets() {
        return this.fallback
                .map(fallback -> this.insets.max(fallback.value().baseInsets()))
                .orElse(this.insets);
    }

    @Override
    public GameMenuInsets insets(ServerPlayer player) {
        return this.insets;
    }

    @Override
    public GameMenuFeatureLayout featureLayout(GameMenuContext ctx) {
        return this.features.orElseGet(() -> GameMenuTheme.super.featureLayout(ctx));
    }

    @Override
    public Component title(GameMenuContext ctx, Component title) {
        var styled = this.title.apply(title);

        var artwork = this.artwork(ctx);
        return artwork == null ? styled : Component.empty().append(artwork).append(styled);
    }

    /**
     * Rebuilt rather than decorated, so the theme owns every line. The entry still owns its click behaviour,
     * which is why the callback is carried over instead of written fresh.
     */
    @Override
    public GuiElement entryElement(GameMenuContext ctx, GameMenuEntry entry) {
        if (this.entry.equals(GameMenuElementStyle.NONE)) {
            return GameMenuTheme.super.entryElement(ctx, entry);
        }

        var element = this.entry.element(entry.icon().copy(), entry.name());

        for (var line : entry.description()) {
            this.entry.addLore(element, line);
        }

        int players = entry.getPlayerCount();
        if (players > -1) {
            int max = entry.getMaxPlayerCount();
            var count = Component.literal(players + (max > 0 ? " / " + max : ""));

            this.entry.addLore(element, Component.translatable("text.plasmid.ui.game_join.players", count));
        }

        var state = entry.getState();
        if (state != null) {
            this.entry.addLore(element, state);
        }

        if (entry.getAction() != GameMenuEntry.Action.NONE) {
            this.entry.addLore(element, entry.getAction().text());
        }

        // Hybrid entries carry a second destination here; a theme drawing its own lore has to ask for it.
        if (entry.getAltAction() != GameMenuEntry.Action.NONE) {
            this.entry.addLore(element, entry.getAltAction().textAlt());
        }

        return element.setCallback(entry.createGuiElement().getGuiCallback()).build();
    }

    @Override
    public GuiElementBuilder chrome(GameMenuContext ctx, GameMenuFeature feature) {
        boolean active = ctx.isActive(feature);
        var appearance = feature.appearance(active);

        var icon = this.icons.get(feature);
        ItemStack stack = icon != null ? icon.create(active) : appearance.icon().create();

        if (this.chrome.equals(GameMenuElementStyle.NONE)) {
            var builder = new GuiElementBuilder(stack).setName(appearance.name()).hideDefaultTooltip();
            return active ? builder.glow() : builder;
        }

        return this.chrome.element(stack, appearance.name());
    }

    /**
     * Panelled like the rest of the menu, but named plainly. The name style marks entries out from their
     * surroundings, which is the opposite of what a placeholder wants.
     */
    @Override
    public GuiElementBuilder emptyElement(GameMenuContext ctx) {
        if (this.empty.isEmpty()) {
            return GameMenuTheme.super.emptyElement(ctx);
        }

        var empty = this.empty.get();
        var text = empty.text().orElseGet(() -> defaultEmptyText(ctx).copy().withStyle(ChatFormatting.GRAY));

        var builder = GuiElementBuilder.from(empty.icon().create())
                .hideDefaultTooltip()
                .setItemName(text);

        this.entry.tooltipStyle().ifPresent(style -> builder.setComponent(DataComponents.TOOLTIP_STYLE, style));

        return builder;
    }

    private static Component defaultEmptyText(GameMenuContext ctx) {
        return ctx.view() == GameMenuContext.View.OPEN_GAMES
                ? Component.translatable("text.plasmid.ui.game_menu.no_open_games")
                : Component.translatable("text.plasmid.ui.game_menu.empty");
    }

    /**
     * Fills every slot the content does not occupy. Skipped entirely when the artwork is showing, since it
     * already draws whatever surrounds the content.
     */
    @Override
    public void decorate(GameMenuContext ctx, Layer chrome) {
        if (this.artwork(ctx) != null) {
            return;
        }

        var frame = ctx.frame();
        var stack = this.filler.map(ItemStackTemplate::create).orElse(null);
        if (stack == null) {
            return;
        }

        var element = new GuiElementBuilder(stack.copy()).hideTooltip().build();

        for (int y = 0; y < chrome.getHeight(); y++) {
            for (int x = 0; x < chrome.getWidth(); x++) {
                if (!frame.contains(x - frame.x(), y - frame.y())) {
                    chrome.setSlot(x + y * chrome.getWidth(), element);
                }
            }
        }
    }

    @Nullable
    private Component artwork(GameMenuContext ctx) {
        return this.background
                .map(background -> background.forRows(ctx.player(), ctx.frame().containerRows()))
                .orElse(null);
    }

    @Override
    public MapCodec<? extends GameMenuTheme> codec() {
        return CODEC;
    }

    /**
     * What a feature's control is drawn with, replacing the icon the feature itself carries. A toggle may
     * name a second icon for when it is engaged.
     */
    public record Icon(ItemStackTemplate idle, Optional<ItemStackTemplate> active) {
        private static final Codec<Icon> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStackTemplate.CODEC.fieldOf("idle").forGetter(Icon::idle),
                ItemStackTemplate.CODEC.optionalFieldOf("active").forGetter(Icon::active)
        ).apply(i, Icon::new));

        /**
         * A bare item is the same icon in both states.
         */
        public static final Codec<Icon> CODEC = Codec.withAlternative(
                FULL_CODEC,
                ItemStackTemplate.CODEC.xmap(idle -> new Icon(idle, Optional.empty()), Icon::idle));

        public ItemStack create(boolean active) {
            return (active ? this.active.orElse(this.idle) : this.idle).create();
        }
    }

    /**
     * What fills the middle of the content region when the current view has nothing to show.
     */
    public record Empty(ItemStackTemplate icon, Optional<Component> text) {
        public static final Codec<Empty> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStackTemplate.CODEC.fieldOf("icon").forGetter(Empty::icon),
                PlasmidCodecs.TEXT.optionalFieldOf("text").forGetter(Empty::text)
        ).apply(i, Empty::new));
    }
}
