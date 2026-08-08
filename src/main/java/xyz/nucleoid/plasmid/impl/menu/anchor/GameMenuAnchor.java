package xyz.nucleoid.plasmid.impl.menu.anchor;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.menu.GameMenuEntries;
import xyz.nucleoid.plasmid.impl.menu.GameMenuRenderer;

/**
 * A sign or entity showing a {@link GameMenuEntry}.
 * <p>
 * The entry is stored on the block or entity itself, as a registry reference or written out in place, so
 * anything a menu can hold can be put in the world, including a {@code plasmid:hybrid} for a different
 * action on right-click.
 * <p>
 * An anchor may also carry a theme, which is then forced on whatever it opens. Stored alongside the entry
 * rather than named by the menu, so the same menu can be a plain one from the lobby and a dressed one from
 * the feature wall.
 */
public interface GameMenuAnchor {
    String NBT_KEY = Plasmid.id("menu_entry").toString();
    String THEME_NBT_KEY = Plasmid.id("menu_theme").toString();

    void setAnchoredEntry(@Nullable Holder<GameMenuEntryConfig> entry);

    @Nullable
    Holder<GameMenuEntryConfig> getAnchoredEntry();

    void setAnchoredTheme(@Nullable Holder<GameMenuTheme> theme);

    /**
     * The theme menus opened from here are drawn in, or {@code null} to use whatever the server picks.
     */
    @Nullable
    Holder<GameMenuTheme> getAnchoredTheme();

    void setDisplay(GameMenuDisplay display);

    @Nullable
    GameMenuDisplay getLastDisplay();

    void setLastDisplay(@Nullable GameMenuDisplay display);

    /**
     * Instances shared across the server, so two anchors on the same entry see the same live state.
     */
    @Nullable
    default GameMenuEntry resolveEntry() {
        var holder = this.getAnchoredEntry();
        return holder == null ? null : GameMenuEntries.of(holder.value());
    }

    default boolean interactWithAnchor(ServerPlayer player) {
        return this.onInteract(player, false);
    }

    default boolean onInteract(ServerPlayer player, boolean alt) {
        var entry = this.resolveEntry();
        if (entry == null) {
            return false;
        }

        var theme = this.getAnchoredTheme();
        if (theme == null) {
            entry.click(player, alt);
        } else {
            // Around the click rather than passed to it: an entry may open a menu of its own making, and a
            // sign bound to one should still be drawn in the style it was bound with.
            GameMenuRenderer.openWithTheme(player, theme.value(), () -> entry.click(player, alt));
        }

        return true;
    }

    default void bind(@Nullable Holder<GameMenuEntryConfig> entry) {
        this.bind(entry, null);
    }

    /**
     * @param theme forces the theme menus opened from here are drawn in, or {@code null} to leave it to the
     * server. Dropped along with the entry when unbinding.
     */
    default void bind(@Nullable Holder<GameMenuEntryConfig> entry, @Nullable Holder<GameMenuTheme> theme) {
        this.setAnchoredEntry(entry);
        this.setAnchoredTheme(entry == null ? null : theme);
        this.setLastDisplay(null);

        if (entry != null) {
            GameMenuAnchors.bind(this);
        } else {
            GameMenuAnchors.unbind(this);
        }
    }

    default void serializeAnchor(ValueOutput root) {
        var entry = this.getAnchoredEntry();
        if (entry != null) {
            root.store(NBT_KEY, GameMenuEntryConfig.ENTRY_CODEC, entry);
        }

        var theme = this.getAnchoredTheme();
        if (theme != null) {
            root.store(THEME_NBT_KEY, GameMenuTheme.ENTRY_CODEC, theme);
        }
    }

    default void deserializeAnchor(ValueInput root) {
        this.bind(
                root.read(NBT_KEY, GameMenuEntryConfig.ENTRY_CODEC).orElse(null),
                root.read(THEME_NBT_KEY, GameMenuTheme.ENTRY_CODEC).orElse(null));
    }

    default boolean updateImmediately() {
        return true;
    }
}
