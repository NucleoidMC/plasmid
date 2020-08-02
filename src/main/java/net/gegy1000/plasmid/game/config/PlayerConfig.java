package net.gegy1000.plasmid.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.NumberCodecs;

public final class PlayerConfig {
    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                NumberCodecs.rangedInt(1, Integer.MAX_VALUE).fieldOf("min").forGetter(PlayerConfig::getMinPlayers),
                NumberCodecs.rangedInt(1, Integer.MAX_VALUE).fieldOf("max").forGetter(PlayerConfig::getMaxPlayers)
        ).apply(instance, PlayerConfig::new);
    });

    private final int minPlayers;
    private final int maxPlayers;

    public PlayerConfig(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }
}
