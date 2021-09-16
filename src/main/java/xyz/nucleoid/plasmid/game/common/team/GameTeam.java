package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.serialization.Codec;

/**
 * An identifier for a specific team for use within various team-related APIs.
 *
 * @see GameTeamConfig
 * @see TeamManager
 * @see TeamSelectionLobby
 */
public final record GameTeam(String id) {
    public static final Codec<GameTeam> CODEC = Codec.STRING.xmap(GameTeam::new, GameTeam::id);
}
