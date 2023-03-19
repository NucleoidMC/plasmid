package xyz.nucleoid.plasmid.game.config;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;

public final class GameConfigs {
    public static final RegistryKey<Registry<GameConfig<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier(Plasmid.ID, "games"));
}
