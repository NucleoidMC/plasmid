package xyz.nucleoid.plasmid.api.game.config;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

public final class GameConfigs {
    public static final RegistryKey<Registry<GameConfig<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(Plasmid.ID, "game"));
}
