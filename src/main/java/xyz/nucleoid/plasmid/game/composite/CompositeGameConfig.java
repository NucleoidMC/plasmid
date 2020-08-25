package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.ArrayList;
import java.util.List;

public final class CompositeGameConfig {
    public static final Codec<CompositeGameConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.listOf().fieldOf("games").forGetter(config -> config.games)
        ).apply(instance, CompositeGameConfig::new);
    });

    private final List<Identifier> games;

    public CompositeGameConfig(List<Identifier> games) {
        this.games = games;
    }

    public List<ConfiguredGame<?>> collectGames() {
        List<ConfiguredGame<?>> games = new ArrayList<>(this.games.size());
        for (Identifier gameId : this.games) {
            ConfiguredGame<?> game = GameConfigs.get(gameId);
            if (game == null) {
                Plasmid.LOGGER.warn("Missing game config by id '{}'!", gameId);
                continue;
            }

            games.add(game);
        }

        return games;
    }
}
