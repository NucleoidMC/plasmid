package xyz.nucleoid.plasmid.game.composite;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public final class GameListConfig {
    public static final Codec<GameListConfig> CODEC = listOrUnitOf(Identifier.CODEC)
            .xmap(GameListConfig::new, config -> config.games);

    private final List<Identifier> games;

    public GameListConfig(List<Identifier> games) {
        this.games = games;
    }

    private static <T> Codec<List<T>> listOrUnitOf(Codec<T> codec) {
        return Codec.either(codec, codec.listOf())
                .xmap(either -> either.map(Collections::singletonList, Function.identity()), Either::right);
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

    @Nullable
    public ConfiguredGame<?> selectGame(Random random) {
        List<ConfiguredGame<?>> games = this.collectGames();
        if (games.isEmpty()) {
            return null;
        }
        return games.get(random.nextInt(games.size()));
    }
}
