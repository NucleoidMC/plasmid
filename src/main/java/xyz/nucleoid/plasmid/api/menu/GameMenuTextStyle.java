package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.Optional;

/**
 * How a {@link GameMenuTheme} dresses one piece of text: a fixed prefix, then colour and weight applied to
 * whatever the menu supplied.
 *
 * <p>The prefix carries its own styling, which is what lets a marker be a different colour from the text it
 * introduces:
 *
 * <pre>{@code
 * { "prefix": { "text": "❯ ", "color": "#9A6BA8" }, "color": "#D65DD6", "bold": true }
 * }</pre>
 *
 * @param italic worth setting to {@code false} for lore, which Minecraft italicises by default
 */
public record GameMenuTextStyle(
        Optional<Component> prefix,
        Optional<TextColor> color,
        Optional<Boolean> bold,
        Optional<Boolean> italic
) {
    /**
     * Passes text through unchanged.
     */
    public static final GameMenuTextStyle NONE =
            new GameMenuTextStyle(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final MapCodec<GameMenuTextStyle> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("prefix").forGetter(GameMenuTextStyle::prefix),
            TextColor.CODEC.optionalFieldOf("color").forGetter(GameMenuTextStyle::color),
            Codec.BOOL.optionalFieldOf("bold").forGetter(GameMenuTextStyle::bold),
            Codec.BOOL.optionalFieldOf("italic").forGetter(GameMenuTextStyle::italic)
    ).apply(i, GameMenuTextStyle::new));

    public static final Codec<GameMenuTextStyle> CODEC = MAP_CODEC.codec();

    /**
     * {@code text} with this style applied, behind the prefix if there is one.
     */
    public MutableComponent apply(Component text) {
        var styled = text.copy().withStyle(style -> style
                .withColor(this.color.orElse(style.getColor()))
                .withBold(this.bold.orElse(style.isBold()))
                .withItalic(this.italic.orElse(style.isItalic())));

        return this.prefix
                .map(prefix -> Component.empty().append(prefix).append(styled))
                .orElse(styled);
    }
}
