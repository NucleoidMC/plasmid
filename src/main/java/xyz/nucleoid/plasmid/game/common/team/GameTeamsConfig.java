package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.serialization.Codec;

import java.util.Map;

public final record GameTeamsConfig(Map<GameTeam, GameTeamConfig> map) {
    public static final Codec<GameTeamsConfig> CODEC = Codec.unboundedMap(GameTeam.CODEC, GameTeamConfig.CODEC)
            .xmap(GameTeamsConfig::new, GameTeamsConfig::map);
}
