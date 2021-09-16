package xyz.nucleoid.plasmid.game.portal;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.function.Function;

public interface GamePortalConfig {
    TinyRegistry<Codec<? extends GamePortalConfig>> REGISTRY = TinyRegistry.create();
    Codec<GamePortalConfig> CODEC = REGISTRY.dispatchStable(GamePortalConfig::codec, Function.identity());

    static void register(Identifier key, Codec<? extends GamePortalConfig> codec) {
        REGISTRY.register(key, codec);
    }

    GamePortalBackend createBackend(MinecraftServer server, Identifier id);

    CustomValuesConfig custom();

    Codec<? extends GamePortalConfig> codec();
}
