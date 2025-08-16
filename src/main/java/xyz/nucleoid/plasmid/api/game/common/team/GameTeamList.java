package xyz.nucleoid.plasmid.api.game.common.team;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public record GameTeamList(List<GameTeam> list) implements Iterable<GameTeam> {
    public static final Codec<GameTeamList> CODEC = GameTeam.CODEC.listOf()
            .xmap(GameTeamList::new, GameTeamList::list);

    @NotNull
    @Override
    public Iterator<GameTeam> iterator() {
        return this.list.iterator();
    }

    public Stream<GameTeam> stream() {
        return this.list.stream();
    }

    @Nullable
    public GameTeam byKey(GameTeamKey key) {
        for (GameTeam team : this.list) {
            if (team.key().equals(key)) {
                return team;
            }
        }
        return null;
    }
}
