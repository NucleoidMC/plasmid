package xyz.nucleoid.plasmid.impl.portal.game;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;

public record ConcurrentGamePortalConfig(RegistryEntry<GameConfig<?>> game, CustomValuesConfig custom) implements GamePortalConfig {
    public static final MapCodec<ConcurrentGamePortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameConfig.CODEC.fieldOf("game").forGetter(c -> c.game),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(c -> c.custom)
    ).apply(i, ConcurrentGamePortalConfig::new));

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        return new ConcurrentGamePortalBackend(this.game);
    }

    @Override
    public MapCodec<ConcurrentGamePortalConfig> codec() {
        return CODEC;
    }
}
