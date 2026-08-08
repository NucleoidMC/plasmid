package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Artwork drawn behind a menu, as a glyph in a resource pack font.
 *
 * <p>A container has no way to show an image, so the picture is smuggled into the title. A space glyph pulls
 * the cursor left to the window's edge, the artwork glyph draws, and a second space pulls back to where the
 * title belongs. The title then prints over it as normal.
 *
 * <p>Containers are only as tall as the menu inside them, so one image cannot serve them all. {@link #glyphs}
 * holds one per height, indexed from a one-row window upwards. A height with no glyph draws nothing.
 *
 * <p>Players without the pack get the plain title, so the menu stays usable either way.
 *
 * <pre>{@code
 * {
 *   "font": "example:menu",
 *   "prefix": "",
 *   "suffix": "",
 *   "glyphs": ["", "", "", "", "", ""]
 * }
 * }</pre>
 *
 * @param font the font carrying the glyphs
 * @param prefix drawn before the artwork, to pull the cursor to the window's edge
 * @param suffix drawn after it, to pull back to where the title belongs
 * @param glyphs one entry per container height, the first being a one-row window
 */
public record GameMenuBackground(Identifier font, String prefix, String suffix, List<String> glyphs) {
    public static final MapCodec<GameMenuBackground> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("font").forGetter(GameMenuBackground::font),
            Codec.STRING.optionalFieldOf("prefix", "").forGetter(GameMenuBackground::prefix),
            Codec.STRING.optionalFieldOf("suffix", "").forGetter(GameMenuBackground::suffix),
            Codec.STRING.listOf().fieldOf("glyphs").forGetter(GameMenuBackground::glyphs)
    ).apply(i, GameMenuBackground::new));

    public static final Codec<GameMenuBackground> CODEC = MAP_CODEC.codec();

    public GameMenuBackground {
        glyphs = List.copyOf(glyphs);
    }

    /**
     * The artwork for a container of this height, or {@code null} if this draws none. Themes should treat a
     * null as "draw the packless fallback", since that is also what a player without the pack gets.
     */
    @Nullable
    public Component forRows(ServerPlayer player, int containerRows) {
        if (!PolymerResourcePackUtils.hasMainPack(player)) {
            return null;
        }

        int index = containerRows - 1;
        if (index < 0 || index >= this.glyphs.size()) {
            return null;
        }

        // White so the pack's own colours come through untinted.
        var style = Style.EMPTY.withColor(0xFFFFFF).withFont(new FontDescription.Resource(this.font));

        return Component.literal(this.prefix + this.glyphs.get(index) + this.suffix).setStyle(style);
    }
}
