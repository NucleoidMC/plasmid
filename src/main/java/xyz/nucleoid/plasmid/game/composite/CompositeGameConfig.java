package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.ConfiguredGame;

import java.util.List;

public final class CompositeGameConfig {
    public static final Codec<CompositeGameConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                GameListConfig.CODEC.fieldOf("games").forGetter(config -> config.games),
                Codec.BOOL.optionalFieldOf("cyclic", false).forGetter(config -> config.cyclic),
                Codec.BOOL.optionalFieldOf("shuffled", true).forGetter(config -> config.shuffled)
        ).apply(instance, CompositeGameConfig::new);
    });

    private final GameListConfig games;
    private final boolean cyclic;
    private final boolean shuffled;

    public CompositeGameConfig(GameListConfig games, boolean cyclic, boolean shuffled) {
        this.games = games;
        this.cyclic = cyclic;
        this.shuffled = shuffled;
    }

    public List<ConfiguredGame<?>> collectGames() {
        return this.games.collectGames();
    }

    public boolean isCyclic() {
        return this.cyclic;
    }

    public boolean isShuffled() {
        return this.shuffled;
    }
}
