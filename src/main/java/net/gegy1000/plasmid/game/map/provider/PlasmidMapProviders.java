package net.gegy1000.plasmid.game.map.provider;

import com.mojang.serialization.Codec;
import net.gegy1000.plasmid.Plasmid;
import net.minecraft.util.Identifier;

public final class PlasmidMapProviders {
    public static void register() {
        register("path", PathMapProvider.CODEC);
        register("random", RandomMapProvider.codec());
    }

    private static <T extends MapProvider<?>> void register(String key, Codec<T> codec) {
        MapProvider.REGISTRY.register(new Identifier(Plasmid.ID, key), codec);
    }
}
