package xyz.nucleoid.plasmid.api.game.common.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A compound object containing both a {@link GameTeamKey} and a {@link GameTeamConfig}.
 *
 * @see GameTeamKey
 * @see GameTeamConfig
 * @see TeamManager
 * @see TeamSelectionLobby
 */
public record GameTeam(
        GameTeamKey key,
        GameTeamConfig config
) {
    public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                GameTeamKey.CODEC.fieldOf("key").forGetter(GameTeam::key),
                GameTeamConfig.MAP_CODEC.forGetter(GameTeam::config)
        ).apply(instance, GameTeam::new);
    });

    public GameTeam withConfig(GameTeamConfig config) {
        return new GameTeam(this.key, config);
    }
}
