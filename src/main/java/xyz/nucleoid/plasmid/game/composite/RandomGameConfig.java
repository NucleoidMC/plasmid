package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.Random;

public final class RandomGameConfig {
    public static final Codec<RandomGameConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                GameListConfig.CODEC.fieldOf("games").forGetter(config -> config.games)
        ).apply(instance, RandomGameConfig::new);
    });

    private final GameListConfig games;

    public RandomGameConfig(GameListConfig games) {
        this.games = games;
    }

    @Nullable
    public GameConfig<?> selectGame(Random random) {
        return this.games.selectGame(random);
    }

    public boolean isEmpty() {
        return this.games.isEmpty();
    }
}
