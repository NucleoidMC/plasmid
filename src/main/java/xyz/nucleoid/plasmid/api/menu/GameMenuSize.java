package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A content region, in container slots.
 * <p>
 * This is what a {@link GameMenu} asks for and what a {@link GameMenuLayout} reports it needs. The theme's
 * {@link GameMenuInsets} are added around it, so a menu states how much room its contents want without
 * knowing what the surrounding look costs.
 * <p>
 * Slots rather than elements: a 9x2 region holds nine elements per row at no padding and five at a padding
 * of one, and which of those applies is the theme's business.
 * <p>
 * Written either way round, whichever reads better where it sits:
 *
 * <pre>{@code
 * "size": "9:4"
 * "size": { "width": 9, "height": 4 }
 * }</pre>
 */
public record GameMenuSize(int width, int height) {
    private static final Codec<GameMenuSize> SHORT_CODEC =
            Codec.STRING.comapFlatMap(GameMenuSize::parse, GameMenuSize::toString);

    private static final Codec<GameMenuSize> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, GameMenuFrame.MAX_COLUMNS).fieldOf("width").forGetter(GameMenuSize::width),
            Codec.intRange(1, GameMenuFrame.MAX_ROWS).fieldOf("height").forGetter(GameMenuSize::height)
    ).apply(i, GameMenuSize::new));

    /**
     * Reads both forms; writes the short one.
     */
    public static final Codec<GameMenuSize> CODEC = Codec.withAlternative(SHORT_CODEC, FULL_CODEC);

    private static DataResult<GameMenuSize> parse(String value) {
        var split = value.split(":");
        if (split.length != 2) {
            return DataResult.error(() -> "Expected a size of the form \"9:4\", got \"" + value + "\"");
        }

        int width;
        int height;
        try {
            width = Integer.parseInt(split[0].trim());
            height = Integer.parseInt(split[1].trim());
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Expected a size of the form \"9:4\", got \"" + value + "\"");
        }

        if (width < 1 || width > GameMenuFrame.MAX_COLUMNS || height < 1 || height > GameMenuFrame.MAX_ROWS) {
            return DataResult.error(() -> "A menu size must fit a vanilla container, between 1:1 and "
                    + GameMenuFrame.MAX_COLUMNS + ":" + GameMenuFrame.MAX_ROWS + ", got \"" + value + "\"");
        }

        return DataResult.success(new GameMenuSize(width, height));
    }

    /**
     * This size cut down to fit {@code max}, never below 1x1.
     */
    public GameMenuSize clampTo(GameMenuSize max) {
        return new GameMenuSize(
                Math.max(1, Math.min(this.width, max.width)),
                Math.max(1, Math.min(this.height, max.height)));
    }

    public boolean fitsIn(GameMenuSize max) {
        return this.width <= max.width && this.height <= max.height;
    }

    @Override
    public String toString() {
        return this.width + ":" + this.height;
    }
}
