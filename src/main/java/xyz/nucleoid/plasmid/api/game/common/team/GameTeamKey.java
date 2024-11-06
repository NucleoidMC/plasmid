package xyz.nucleoid.plasmid.api.game.common.team;

import com.mojang.serialization.Codec;

/**
 * An identifier for a specific team for use within various team-related APIs.
 *
 * @see GameTeam
 * @see GameTeamConfig
 * @see TeamManager
 * @see TeamSelectionLobby
 */
public final record GameTeamKey(String id) {
    public static final Codec<GameTeamKey> CODEC = Codec.STRING.xmap(GameTeamKey::new, GameTeamKey::id);
}
