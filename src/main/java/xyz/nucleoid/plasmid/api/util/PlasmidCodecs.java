package xyz.nucleoid.plasmid.api.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class PlasmidCodecs {
    public static Codec<Component> TEXT = Codec.either(Codec.STRING, ComponentSerialization.CODEC)
            .xmap(either -> either.map((s) -> TagParser.QUICK_TEXT_WITH_STF.parseComponent(s, ParserContext.of()), Function.identity()), Either::right);

    private PlasmidCodecs() {}

}
