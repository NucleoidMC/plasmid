package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.Random;

public record RandomGameConfig(GameListConfig games) {
    public static final Codec<RandomGameConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                GameListConfig.CODEC.fieldOf("games").forGetter(config -> config.games)
        ).apply(instance, RandomGameConfig::new);
    });

    @Nullable
    public GameConfig<?> selectGame(Random random) {
        return this.games.selectGame(random);
    }

    public boolean isEmpty() {
        return this.games.isEmpty();
    }
}
