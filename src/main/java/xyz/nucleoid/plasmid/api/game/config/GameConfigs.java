package xyz.nucleoid.plasmid.api.game.config;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

public final class GameConfigs {
    /**
     * @deprecated Use {@link PlasmidRegistryKeys#GAME_CONFIG} instead.
     */
    @Deprecated
    public static final RegistryKey<Registry<GameConfig<?>>> REGISTRY_KEY = PlasmidRegistryKeys.GAME_CONFIG;
}
