package xyz.nucleoid.plasmid.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.TextParser;
import net.minecraft.text.Text;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.function.Function;

public final class PlasmidCodecs {
    public static Codec<Text> TEXT = Codec.either(Codec.STRING, MoreCodecs.TEXT)
            .xmap(either -> either.map(TextParser::parseSafe, Function.identity()), Either::right);

    private PlasmidCodecs() {}

}
