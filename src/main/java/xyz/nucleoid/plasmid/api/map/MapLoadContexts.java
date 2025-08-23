package xyz.nucleoid.plasmid.api.map;

import net.minecraft.util.context.ContextParameter;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class MapLoadContexts {
	public static final ContextParameter<GameTeamList> TEAM_LIST = of("team_list");

    public static <T> ContextParameter<T> of(String key) {
        return new ContextParameter<>(Plasmid.id(key));
    }
}
