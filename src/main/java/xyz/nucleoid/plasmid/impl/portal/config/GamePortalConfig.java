package xyz.nucleoid.plasmid.impl.portal.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public interface GamePortalConfig {
    TinyRegistry<MapCodec<? extends GamePortalConfig>> REGISTRY = TinyRegistry.create();
    Codec<GamePortalConfig> CODEC = REGISTRY.dispatchStable(GamePortalConfig::codec, Function.identity());
    @Deprecated
    Map<Class<? extends GamePortalConfig>, GamePortalBackend.Factory<?>> FACTORIES = new IdentityHashMap<>();

    static void register(Identifier key, MapCodec<? extends GamePortalConfig> codec) {
        REGISTRY.register(key, codec);
    }
    static <T extends GamePortalConfig> void registerFactory(Class<T> configClass, GamePortalBackend.Factory<T> factory) {
        FACTORIES.put(configClass, factory);
    }

    @Nullable
    static <T extends GamePortalConfig> GamePortalBackend create(MinecraftServer server, Identifier identifier, T config) {
        //noinspection unchecked
        var factory = (GamePortalBackend.Factory<T>) FACTORIES.get(config.getClass());
        if (factory != null) {
            return factory.create(server, identifier, config);
        }
        return null;
    }

    CustomValuesConfig custom();

    MapCodec<? extends GamePortalConfig> codec();
}
