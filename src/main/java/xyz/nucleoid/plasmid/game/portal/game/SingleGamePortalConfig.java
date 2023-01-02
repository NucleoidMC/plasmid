package xyz.nucleoid.plasmid.game.portal.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalConfig;

public record SingleGamePortalConfig(Identifier gameId, CustomValuesConfig custom) implements GamePortalConfig {
    public static final Codec<SingleGamePortalConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("game").forGetter(c -> c.gameId),
                CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(c -> c.custom)
        ).apply(instance, SingleGamePortalConfig::new);
    });

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        return new SingleGamePortalBackend(this.gameId);
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
