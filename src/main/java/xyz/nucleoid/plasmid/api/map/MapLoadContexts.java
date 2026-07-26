package xyz.nucleoid.plasmid.api.map;

import net.minecraft.util.context.ContextKey;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class MapLoadContexts {
	public static final ContextKey<GameTeamList> TEAM_LIST = of("team_list");

    public static <T> ContextKey<T> of(String key) {
        return new ContextKey<>(Plasmid.id(key));
    }
}
