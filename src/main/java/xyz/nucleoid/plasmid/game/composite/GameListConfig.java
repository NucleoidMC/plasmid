package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.registry.TinyEntry;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.*;

public record GameListConfig(TinyEntry<TinyRegistry.EntryKey<?>, Collection<GameConfig<?>>> games) {
    public static final Codec<GameListConfig> CODEC = GameConfigs.getEntryCodec()
            .xmap(GameListConfig::new, config -> config.games);

    public List<GameConfig<?>> collectGames() {
        return List.copyOf(this.games.orElse(List::of));
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
        return this.games.orElse(List::of).isEmpty();
    }
}
