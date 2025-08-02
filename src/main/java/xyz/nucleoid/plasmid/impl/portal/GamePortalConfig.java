package xyz.nucleoid.plasmid.impl.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.portal.GamePortalConfigs;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.function.Function;

public interface GamePortalConfig {
    /**
     * @deprecated Use {@link PlasmidRegistries#GAME_PORTAL_CONFIG} instead.
     */
    @Deprecated
    Registry<MapCodec<? extends GamePortalConfig>> REGISTRY = PlasmidRegistries.GAME_PORTAL_CONFIG;
    Codec<GamePortalConfig> CODEC = PlasmidRegistries.GAME_PORTAL_CONFIG.getCodec().dispatchStable(GamePortalConfig::codec, Function.identity());

    /**
     * @deprecated Use {@link GamePortalConfigs#register(Identifier, MapCodec)} instead.
     */
    @Deprecated
    static MapCodec<? extends GamePortalConfig> register(Identifier key, MapCodec<? extends GamePortalConfig> codec) {
        return GamePortalConfigs.register(key, codec);
    }

    GamePortalBackend createBackend(MinecraftServer server, Identifier id);

    CustomValuesConfig custom();

    MapCodec<? extends GamePortalConfig> codec();
}
