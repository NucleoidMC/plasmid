package xyz.nucleoid.plasmid.game.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.function.Function;

public interface GamePortalConfig {
    TinyRegistry<MapCodec<? extends GamePortalConfig>> REGISTRY = TinyRegistry.create();
    Codec<GamePortalConfig> CODEC = REGISTRY.dispatchStable(GamePortalConfig::codec, Function.identity());

    static void register(Identifier key, MapCodec<? extends GamePortalConfig> codec) {
        REGISTRY.register(key, codec);
    }

    GamePortalBackend createBackend(MinecraftServer server, Identifier id);

    CustomValuesConfig custom();

    MapCodec<? extends GamePortalConfig> codec();
}
