package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * What a {@link GameMenuTheme} keeps for itself around a menu's content: rows above and below, columns
 * either side, and the spacing it leaves between elements within.
 *
 * <p>These are added to the {@link GameMenuSize} the menu asks for, not carved out of it. A 9:4 menu is
 * drawn in a 9:6 container under a theme keeping a row above and below, and in a 9:4 one under a theme
 * keeping nothing. The menu decides how much room its contents need; the theme decides what surrounds them.
 *
 * <p>Padding belongs here for the same reason the outer rows do. A game says "these five things, arranged
 * dynamically"; the server says whether they sit shoulder to shoulder or spread out.
 *
 * <p>Values are clamped to leave at least one content row and column, so no theme can reserve a container
 * out of existence.
 *
 * @param top rows kept above the content
 * @param bottom rows kept below the content, where the feature bar normally sits
 * @param left columns kept to the left of the content
 * @param right columns kept to the right of the content
 * @param paddingX empty slots between horizontally adjacent elements
 * @param paddingY empty rows between element rows
 */
public record GameMenuInsets(int top, int bottom, int left, int right, int paddingX, int paddingY) {
    /**
     * A theme that draws no chrome of its own: the menu fills the container.
     */
    public static final GameMenuInsets NONE = new GameMenuInsets(0, 0, 0, 0, 0, 0);

    public static final MapCodec<GameMenuInsets> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(0, GameMenuFrame.MAX_ROWS - 1).optionalFieldOf("top", 0).forGetter(GameMenuInsets::top),
            Codec.intRange(0, GameMenuFrame.MAX_ROWS - 1).optionalFieldOf("bottom", 0).forGetter(GameMenuInsets::bottom),
            Codec.intRange(0, GameMenuFrame.MAX_COLUMNS - 1).optionalFieldOf("left", 0).forGetter(GameMenuInsets::left),
            Codec.intRange(0, GameMenuFrame.MAX_COLUMNS - 1).optionalFieldOf("right", 0).forGetter(GameMenuInsets::right),
            Codec.intRange(0, GameMenuFrame.MAX_COLUMNS - 1).optionalFieldOf("padding_x", 0).forGetter(GameMenuInsets::paddingX),
            Codec.intRange(0, GameMenuFrame.MAX_ROWS - 1).optionalFieldOf("padding_y", 0).forGetter(GameMenuInsets::paddingY)
    ).apply(i, GameMenuInsets::new));

    public GameMenuInsets {
        top = Mth.clamp(top, 0, GameMenuFrame.MAX_ROWS - 1);
        bottom = Mth.clamp(bottom, 0, GameMenuFrame.MAX_ROWS - 1 - top);
        left = Mth.clamp(left, 0, GameMenuFrame.MAX_COLUMNS - 1);
        right = Mth.clamp(right, 0, GameMenuFrame.MAX_COLUMNS - 1 - left);
        paddingX = Math.max(0, paddingX);
        paddingY = Math.max(0, paddingY);
    }

    /**
     * A bar above, below, or both, which is what most themes want.
     */
    public static GameMenuInsets ofRows(int top, int bottom) {
        return new GameMenuInsets(top, bottom, 0, 0, 0, 0);
    }

    public GameMenuInsets max(GameMenuInsets other) {
        return new GameMenuInsets(
                Math.max(this.top, other.top),
                Math.max(this.bottom, other.bottom),
                Math.max(this.left, other.left),
                Math.max(this.right, other.right),
                Math.max(this.paddingX, other.paddingX),
                Math.max(this.paddingY, other.paddingY));
    }

    public int horizontal() {
        return this.left + this.right;
    }

    public int vertical() {
        return this.top + this.bottom;
    }

    /**
     * The largest content region left over in a vanilla container. Always at least 1x1.
     */
    public GameMenuSize available() {
        return new GameMenuSize(
                GameMenuFrame.MAX_COLUMNS - this.horizontal(),
                GameMenuFrame.MAX_ROWS - this.vertical());
    }

    /**
     * Phrased to follow "its ...", for the log line a menu that does not fit earns.
     */
    public String describe() {
        var parts = new ArrayList<String>();
        add(parts, this.top, "row", "above");
        add(parts, this.bottom, "row", "below");
        add(parts, this.left, "column", "to the left");
        add(parts, this.right, "column", "to the right");

        return switch (parts.size()) {
            case 0 -> "no chrome";
            case 1 -> parts.getFirst();
            default -> String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.getLast();
        };
    }

    private static void add(List<String> parts, int size, String unit, String where) {
        if (size > 0) {
            parts.add(size + " " + unit + (size > 1 ? "s " : " ") + where);
        }
    }
}
