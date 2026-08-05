package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where a {@link GameMenuTheme} puts each feature's control, as container cells.
 *
 * <p>The counterpart to {@link GameMenuLayout}. That one arranges what the game put in the menu; this one
 * arranges the chrome the theme draws around it. Positions are container cells rather than content-region
 * ones, so each control is placed on its own and the close button can sit in the opposite corner from the
 * rest.
 *
 * <p>A feature this does not place is not drawn. Features {@link GameMenuContext#isPinned pinned} by the
 * menu's own layout are placed there instead and ignored here.
 *
 * <p>A negative coordinate counts back from the far edge, {@code -1} being the last column or row.
 * Containers are only as tall as the menu inside them needs, so a bar written against the bottom edge stays
 * there whether that is row 2 or row 5.
 *
 * <p>In JSON, a map of feature id to {@code "x:y"}:
 *
 * <pre>{@code
 * { "plasmid:game_filter": "4:-1", "plasmid:back": "-1:-1", "plasmid:close": "0:-1" }
 * }</pre>
 */
public record GameMenuFeatureLayout(Map<GameMenuFeature, GameMenuLayout.Pos> slots) {
    /**
     * Places nothing. A theme returning this draws no chrome controls at all.
     */
    public static final GameMenuFeatureLayout EMPTY = new GameMenuFeatureLayout(Map.of());

    public static final Codec<GameMenuFeatureLayout> CODEC =
            Codec.unboundedMap(GameMenuFeature.CODEC, GameMenuLayout.Pos.CODEC)
                    .xmap(GameMenuFeatureLayout::new, GameMenuFeatureLayout::slots);

    public GameMenuFeatureLayout {
        slots = Map.copyOf(slots);
    }

    /**
     * The arrangement a {@link GameMenuTheme} gets without saying otherwise: a bar in the row the theme kept
     * below the content, page controls flanking the game filter, back at the far end.
     *
     * <p>Read off the resolved frame, so it lands correctly whatever height the container ended up. A theme
     * keeping a row above the content but none below gets its bar up there instead. A theme keeping no rows
     * at all gets no bar, since anywhere it could go would cover an entry.
     */
    public static GameMenuFeatureLayout standard(GameMenuFrame frame) {
        int below = frame.y() + frame.height();
        int row = below < frame.containerRows() ? below : frame.y() - 1;

        if (row < 0) {
            return EMPTY;
        }

        return EMPTY
                .with(GameMenuFeatures.PREVIOUS_PAGE, 2, row)
                .with(GameMenuFeatures.GAME_FILTER, 4, row)
                .with(GameMenuFeatures.NEXT_PAGE, 6, row)
                .with(GameMenuFeatures.BACK, 8, row);
    }

    /**
     * This layout with {@code feature} at the given container cell, replacing any position it already had.
     */
    public GameMenuFeatureLayout with(GameMenuFeature feature, int x, int y) {
        var slots = new LinkedHashMap<>(this.slots);
        slots.put(feature, new GameMenuLayout.Pos(x, y));
        return new GameMenuFeatureLayout(slots);
    }

    /**
     * This layout with {@code feature} unplaced, and so undrawn.
     */
    public GameMenuFeatureLayout without(GameMenuFeature feature) {
        if (!this.slots.containsKey(feature)) {
            return this;
        }

        var slots = new LinkedHashMap<>(this.slots);
        slots.remove(feature);
        return new GameMenuFeatureLayout(slots);
    }

    /**
     * The cell this places {@code feature} at, before negative coordinates are resolved.
     */
    @Nullable
    public GameMenuLayout.Pos pos(GameMenuFeature feature) {
        return this.slots.get(feature);
    }

    /**
     * The container slot this puts {@code feature} in, or {@code -1} if it places it nowhere or outside the
     * container. Negative coordinates are resolved against the given size.
     */
    public int slot(GameMenuFeature feature, int containerWidth, int containerRows) {
        var pos = this.slots.get(feature);
        if (pos == null) {
            return -1;
        }

        int x = pos.x() < 0 ? containerWidth + pos.x() : pos.x();
        int y = pos.y() < 0 ? containerRows + pos.y() : pos.y();

        if (x < 0 || x >= containerWidth || y < 0 || y >= containerRows) {
            return -1;
        }

        return x + y * containerWidth;
    }
}
