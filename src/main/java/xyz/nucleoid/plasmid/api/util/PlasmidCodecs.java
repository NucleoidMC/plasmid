package xyz.nucleoid.plasmid.api.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public final class PlasmidCodecs {
    public static Codec<Text> TEXT = Codec.either(Codec.STRING, TextCodecs.CODEC)
            .xmap(either -> either.map((s) -> TagParser.QUICK_TEXT_WITH_STF.parseText(s, ParserContext.of()), Function.identity()), Either::right);

    private PlasmidCodecs() {}

}
