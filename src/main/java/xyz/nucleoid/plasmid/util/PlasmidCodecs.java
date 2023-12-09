package xyz.nucleoid.plasmid.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public final class PlasmidCodecs {
    public static Codec<Text> TEXT = Codec.either(Codec.STRING, TextCodecs.CODEC)
            .xmap(either -> either.map(TextParserUtils::formatTextSafe, Function.identity()), Either::right);

    private PlasmidCodecs() {}

}
