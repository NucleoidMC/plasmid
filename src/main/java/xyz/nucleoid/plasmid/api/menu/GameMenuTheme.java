package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

import java.util.function.Function;

/**
 * How a {@link GameMenu} is drawn, kept separate from what it contains.
 *
 * <p>Games never name a theme. The server picks one, and a menu opened from another inherits it, so a game
 * is developed against the plain built-in look and picks up the server's in production unchanged.
 *
 * <p>A theme does not decide how big a menu is. The menu asks for a {@link GameMenuSize}, the theme says
 * what it keeps around it through {@link GameMenuInsets}, and the container is grown to hold both. The same
 * theme therefore dresses a two-row menu and a six-row one.
 *
 * <p>Instances load from {@code data/<namespace>/plasmid/game_menu_theme/*.json}, dispatched on
 * {@code type}. Whichever loads under {@code plasmid:default} is the server default, so a datapack replaces
 * the whole look by writing that path.
 *
 * <p>Implementations must be stateless: everything they need arrives on the {@link GameMenuContext}.
 *
 * @see GameMenuThemeTypes
 */
public interface GameMenuTheme {
    Codec<GameMenuTheme> DIRECT_CODEC = PlasmidRegistries.GAME_MENU_THEME_TYPE.byNameCodec().dispatchStable(GameMenuTheme::codec, Function.identity());

    Codec<Holder<GameMenuTheme>> ENTRY_CODEC = RegistryFileCodec.create(PlasmidRegistryKeys.GAME_MENU_THEME, DIRECT_CODEC);

    /**
     * The theme to actually draw this player with, chosen once when the menu opens.
     *
     * <p>Override to hand the whole look over to another theme, which is how a theme built on resource pack
     * artwork falls back for players without the pack. Handing over the theme rather than varying each piece
     * keeps the two looks from being mixed: the replacement supplies its own insets, features and elements,
     * and everything opened from the menu inherits it.
     *
     * <p>Called once per menu, so a chain of themes each handing over resolves a single step.
     */
    default GameMenuTheme forPlayer(ServerPlayer player) {
        return this;
    }

    /**
     * The rows and columns this keeps for its own chrome, added around whatever region the menu asks for.
     * Independent of any player; menus are validated against this at datapack load.
     *
     * <p>A theme that hands over through {@link #forPlayer} should report enough room for both looks, so a
     * menu that would not fit either is reported at load rather than when the right player opens it. Such a
     * theme must also override {@link #insets(ServerPlayer)}, which otherwise defaults to this and would
     * draw the combined chrome rather than its own.
     */
    GameMenuInsets baseInsets();

    /**
     * The insets to draw with. Override to vary them per player, typically on resource pack presence.
     * Validation still uses {@link #baseInsets()}, so a menu that fits there may still be cut down here.
     */
    default GameMenuInsets insets(ServerPlayer player) {
        return this.baseInsets();
    }

    /**
     * Whether the player inventory is part of the addressable area.
     */
    default boolean includePlayerSlots() {
        return false;
    }

    /**
     * Whether contents too big for the region may be paged rather than dropped.
     */
    default boolean paginated() {
        return true;
    }

    /**
     * Decorates the menu title. Resource pack backed themes prefix it with the characters drawing their
     * background texture.
     */
    default Component title(GameMenuContext ctx, Component title) {
        return title;
    }

    /**
     * The appearance of a feature's control, or {@code null} to hide it. Appearance only: Plasmid attaches
     * the callback.
     */
    @Nullable
    default GuiElementBuilder chrome(GameMenuContext ctx, GameMenuFeature feature) {
        boolean active = ctx.isActive(feature);
        var appearance = feature.appearance(active);

        var builder = new GuiElementBuilder(appearance.icon().create())
                .setName(appearance.name())
                .hideDefaultTooltip();

        if (active) {
            builder.glow();
        }

        return builder;
    }

    /**
     * The appearance of an entry. Override to restyle names, lore or icons across every menu at once.
     *
     * <p>Unlike {@link #chrome}, the callback belongs to the entry, and some entries bind right-click to a
     * second action. Carry {@code entry.createGuiElement().getGuiCallback()} over rather than writing one.
     */
    default GuiElement entryElement(GameMenuContext ctx, GameMenuEntry entry) {
        return entry.createGuiElement();
    }

    /**
     * Shown in the middle of the content region when the current view is empty, most often open-games with
     * no game running. Return {@code null} to leave it blank.
     */
    @Nullable
    default GuiElementBuilder emptyElement(GameMenuContext ctx) {
        var reason = ctx.view() == GameMenuContext.View.OPEN_GAMES
                ? Component.translatable("text.plasmid.ui.game_menu.no_open_games")
                : Component.translatable("text.plasmid.ui.game_menu.empty");

        return new GuiElementBuilder(Items.BARRIER)
                .setItemName(reason.copy().withStyle(ChatFormatting.GRAY))
                .hideDefaultTooltip();
    }

    /**
     * Where the feature controls go. Each is placed on its own, so a theme may sit the close button in the
     * opposite corner from the rest, or leave a feature out by not placing it.
     *
     * <p>Defaults to {@link GameMenuFeatureLayout#standard}. Override to arrange them differently, or to
     * add to that arrangement:
     *
     * <pre>{@code
     * GameMenuTheme.super.featureLayout(ctx).with(GameMenuFeatures.CLOSE, 0, -1)
     * }</pre>
     */
    default GameMenuFeatureLayout featureLayout(GameMenuContext ctx) {
        return GameMenuFeatureLayout.standard(ctx.frame());
    }

    /**
     * Fills the chrome layer, which spans the container and sits behind the content: backgrounds, filler,
     * navigation bar. Called before Plasmid stamps the feature controls on top.
     */
    default void decorate(GameMenuContext ctx, Layer chrome) {
    }

    MapCodec<? extends GameMenuTheme> codec();
}
