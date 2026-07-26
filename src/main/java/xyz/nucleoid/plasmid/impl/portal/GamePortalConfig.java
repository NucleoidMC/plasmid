package xyz.nucleoid.plasmid.impl.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.portal.GamePortalConfigs;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

import java.util.function.Function;

public interface GamePortalConfig {
    /**
     * @deprecated Use {@link PlasmidRegistries#GAME_PORTAL_CONFIG} instead.
     */
    Codec<GamePortalConfig> CODEC = PlasmidRegistries.GAME_PORTAL_CONFIG.byNameCodec().dispatchStable(GamePortalConfig::codec, Function.identity());

    GamePortalBackend createBackend(MinecraftServer server, Identifier id);

    CustomValuesConfig custom();

    MapCodec<? extends GamePortalConfig> codec();
}
