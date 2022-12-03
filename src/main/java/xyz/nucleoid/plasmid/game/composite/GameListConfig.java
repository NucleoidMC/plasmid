package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record GameListConfig(List<Identifier> games) {
    public static final Codec<GameListConfig> CODEC = MoreCodecs.listOrUnit(Identifier.CODEC)
            .xmap(GameListConfig::new, config -> config.games);

    public List<GameConfig<?>> collectGames() {
        var games = new ArrayList<GameConfig<?>>(this.games.size());
        for (var gameId : this.games) {
            var game = GameConfigs.get().byKey(gameId);
            if (game == null) {
                Plasmid.LOGGER.warn("Missing game config by id '{}'!", gameId);
                continue;
            }
            games.add(game);
        }
        return games;
    }

    @Nullable
    public GameConfig<?> selectGame(Random random) {
        var games = this.collectGames();
        if (games.isEmpty()) {
            return null;
        }
        return games.get(random.nextInt(games.size()));
    }

    public boolean isEmpty() {
        return this.games.isEmpty();
    }
}
