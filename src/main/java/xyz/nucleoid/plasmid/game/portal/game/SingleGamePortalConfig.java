package xyz.nucleoid.plasmid.game.portal.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalConfig;

public record SingleGamePortalConfig(RegistryEntry<GameConfig<?>> game, CustomValuesConfig custom) implements GamePortalConfig {
    public static final Codec<SingleGamePortalConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            GameConfig.CODEC.fieldOf("game").forGetter(c -> c.game),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(c -> c.custom)
    ).apply(i, SingleGamePortalConfig::new));

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        return new SingleGamePortalBackend(this.game);
    }

    @Override
    public CustomValuesConfig custom() {
        return this.custom;
    }

    @Override
    public Codec<? extends GamePortalConfig> codec() {
        return CODEC;
    }
}
