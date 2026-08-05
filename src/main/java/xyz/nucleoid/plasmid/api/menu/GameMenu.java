package xyz.nucleoid.plasmid.api.menu;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.menu.GameMenuRenderer;

import java.util.*;
import java.util.function.Consumer;

/**
 * A menu of {@link GameMenuEntry entries}, described independently of how it is drawn.
 *
 * <p>A game supplies a title, a {@link GameMenuLayout}, how much room it wants and the
 * {@link GameMenuFeature features} it needs. The server's {@link GameMenuTheme} supplies the surrounding chrome
 * and the container is sized to hold both.
 *
 * <pre>{@code
 * GameMenu.builder(Component.translatable("uhc.menu.custom"))
 *         .layout(GameMenuLayout.rows(
 *                 List.of(modeToggle, teamSize),
 *                 List.of(worldBorder, pvpDelay)))
 *         .features()
 *         .open(player);
 * }</pre>
 *
 * @see GameMenuTheme
 */
public final class GameMenu {
    private final Component title;
    private final GameMenuLayout<GameMenuElement> layout;
    private final GameMenuSizeRule sizeRule;
    @Nullable
    private final Set<GameMenuFeature> declaredFeatures;
    @Nullable
    private final Holder<GameMenuTheme> theme;

    private GameMenu(Component title, GameMenuLayout<GameMenuElement> layout, GameMenuSizeRule sizeRule, @Nullable Set<GameMenuFeature> declaredFeatures, @Nullable Holder<GameMenuTheme> theme) {
        this.title = title;
        this.layout = layout;
        this.sizeRule = sizeRule;
        this.declaredFeatures = declaredFeatures;
        this.theme = theme;
    }

    public static Builder builder(Component title) {
        return new Builder(title);
    }

    public Component title() {
        return this.title;
    }

    public GameMenuLayout<GameMenuElement> layout() {
        return this.layout;
    }

    /**
     * How this menu decides how much room to ask for. Whatever it settles on, the theme's chrome is added
     * around it rather than eating into it.
     */
    public GameMenuSizeRule sizeRule() {
        return this.sizeRule;
    }

    /**
     * The declared features, or those derived from the contents if none were declared. Contextual features
     * such as {@link GameMenuFeatures#BACK} are added per player; see {@link GameMenuContext#features()}.
     */
    public Set<GameMenuFeature> features() {
        if (this.declaredFeatures != null) {
            return this.declaredFeatures;
        }

        return this.hasGameEntries() ? Set.of(GameMenuFeatures.GAME_FILTER) : Set.of();
    }

    /**
     * A theme requested by this menu, taking precedence over the one inherited from the opening menu.
     */
    public Optional<Holder<GameMenuTheme>> themeOverride() {
        return Optional.ofNullable(this.theme);
    }

    /**
     * Whether any entry leads to a game, and so whether the open-games filter is meaningful.
     */
    public boolean hasGameEntries() {
        for (var element : this.layout.elements()) {
            if (element instanceof GameMenuElement.Entry(GameMenuEntry entry) && entry.isGameEntry()) {
                return true;
            }
        }

        return false;
    }

    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        for (var element : this.layout.elements()) {
            if (element instanceof GameMenuElement.Entry(GameMenuEntry entry)) {
                entry.provideGameSpaces(consumer);
            }
        }
    }

    /**
     * Opens this menu, resolving its theme through {@link GameMenuThemeTypes#resolve}.
     */
    public void open(ServerPlayer player) {
        GameMenuRenderer.open(this, player);
    }

    public static final class Builder {
        private final Component title;
        private GameMenuLayout<GameMenuElement> layout = GameMenuLayout.flow(List.of());
        private GameMenuSizeRule sizeRule = GameMenuSizeRule.Auto.FIT;
        @Nullable
        private Set<GameMenuFeature> features;
        @Nullable
        private Holder<GameMenuTheme> theme;

        private Builder(Component title) {
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder layout(GameMenuLayout<GameMenuElement> layout) {
            this.layout = Objects.requireNonNull(layout, "layout");
            return this;
        }

        /**
         * Asks for a content region of exactly this size, instead of letting the layout work one out. Worth
         * doing when a menu should keep its shape however few entries it currently holds.
         */
        public Builder size(int width, int height) {
            return this.sizeRule(GameMenuSizeRule.of(width, height));
        }

        /**
         * {@link GameMenuSizeRule.Auto#FIT} unless said otherwise.
         */
        public Builder sizeRule(GameMenuSizeRule sizeRule) {
            this.sizeRule = Objects.requireNonNull(sizeRule, "sizeRule");
            return this;
        }

        /**
         * Shorthand for a {@link xyz.nucleoid.plasmid.api.menu.layout.FlowLayout FlowLayout} over entries.
         */
        public Builder entries(List<? extends GameMenuEntry> entries) {
            return this.layout(GameMenuLayout.flow(entries.stream().map(GameMenuElement::of).toList()));
        }

        /**
         * Declares the exact feature set, replacing auto-derivation. Passing none opts a menu holding game
         * entries out of the open-games filter.
         */
        public Builder features(GameMenuFeature... features) {
            this.features = new LinkedHashSet<>(List.of(features));
            return this;
        }

        /**
         * Forces a theme instead of inheriting. Rarely what a game wants, but can be useful for custom entries.
         */
        public Builder theme(@Nullable Holder<GameMenuTheme> theme) {
            this.theme = theme;
            return this;
        }

        public GameMenu build() {
            return new GameMenu(this.title, this.layout, this.sizeRule, this.features, this.theme);
        }

        public void open(ServerPlayer player) {
            this.build().open(player);
        }
    }
}
