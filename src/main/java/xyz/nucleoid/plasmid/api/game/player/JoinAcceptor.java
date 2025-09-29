package xyz.nucleoid.plasmid.api.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.util.PlayerPos;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an agent which is responsible for bringing a player or group of players
 * into the {@link GameSpace} world in the correct location.
 * <p>
 * This object should be used in order to construct a {@link JoinAcceptorResult} object to return from a listener to the
 * {@link GamePlayerEvents#ACCEPT} event.
 *
 * @see GameSpace
 * @see GamePlayerEvents#ACCEPT
 */
public interface JoinAcceptor {
    /**
     * @return the set of {@link GameProfile} of the players that are joining to this {@link GameSpace}
     */
    Set<GameProfile> players();

    /**
     * @return the {@link UUID profile UUID} of the players that are joining to this {@link GameSpace}
     */
    default Set<UUID> playerIds() {
        return this.players()
                .stream()
                .map(GameProfile::id)
                .collect(Collectors.toSet());
    }

    /**
     * @return the usernames of the players that are joining to this {@link GameSpace}
     */
    default Set<String> playerNames() {
        return this.players()
                .stream()
                .map(GameProfile::name)
                .collect(Collectors.toSet());
    }

    /**
     * @return the {@link JoinIntent 'intent'} of the players, such as whether they want to participate or spectate
     * @see JoinIntent
     */
    JoinIntent intent();

    /**
     * Returns a result that completes this join by teleporting the players.
     * <p>
     * The result of this function must be returned within a
     * {@link GamePlayerEvents#ACCEPT} listener.
     *
     * @param positions the map of positions where the players should be teleported to
     * @return a "teleport" result
     * @throws IllegalArgumentException when positions are not specified for all joining players
     * @see JoinAcceptorResult.Teleport#thenRun(Consumer)
     * @see JoinAcceptorResult.Teleport#thenRunForEach(Consumer)
     */
    JoinAcceptorResult.Teleport teleport(Map<UUID, PlayerPos> positions);

    /**
     * Returns a result that completes this join by teleporting the players.
     * <p>
     * The result of this function must be returned within a
     * {@link GamePlayerEvents#ACCEPT} listener.
     *
     * @param positions a function that for given player returns position where the player should be teleported to
     * @return a "teleport" result
     * @throws IllegalArgumentException when positions are not specified for all joining players
     * @see JoinAcceptorResult.Teleport#thenRun(Consumer)
     * @see JoinAcceptorResult.Teleport#thenRunForEach(Consumer)
     */
    JoinAcceptorResult.Teleport teleport(Function<GameProfile, PlayerPos> positions);

    /**
     * Returns a result that completes this join by teleporting the players.
     * <p>
     * The result of this function must be returned within a
     * {@link GamePlayerEvents#ACCEPT} listener.
     *
     * @param world the world that all the players should be teleported to
     * @param position the position that all the players should be teleported to
     * @param yaw the 'yaw' angle that all the players should be teleported to
     * @param pitch the 'pitch' angle that all the players should be teleported to
     * @return a "teleport" result
     * @see JoinAcceptorResult.Teleport#thenRun(Consumer)
     * @see JoinAcceptorResult.Teleport#thenRunForEach(Consumer)
     */
    JoinAcceptorResult.Teleport teleport(ServerWorld world, Vec3d position, float yaw, float pitch);

    /**
     * Returns a result that completes this join by teleporting the players.
     * <p>
     * The result of this function must be returned within a
     * {@link GamePlayerEvents#ACCEPT} listener.
     *
     * @param world the world that all the players should be teleported to
     * @param position the position that all the players should be teleported to
     * @return a "teleport" result
     * @see JoinAcceptorResult.Teleport#thenRun(Consumer)
     * @see JoinAcceptorResult.Teleport#thenRunForEach(Consumer)
     */
    default JoinAcceptorResult.Teleport teleport(ServerWorld world, Vec3d position) {
        return this.teleport(world, position, 0, 0);
    }

    /**
     * Returns a result that does nothing, passing on any handling to any other listener.
     *
     * @return a "passing" result
     */
    default JoinAcceptorResult pass() {
        return JoinAcceptorResult.PASS;
    }

    /**
     * The result of this function only is selected for spectators, returning pass for non-spectators
     * {@link GamePlayerEvents#ACCEPT} listener.
     * @return a result
     */
    default JoinAcceptorResult ifSpectator(Function<JoinAcceptor, JoinAcceptorResult> spectatorFunction) {
        if (this.intent() == JoinIntent.SPECTATE) {
            return spectatorFunction.apply(this);
        } else {
            return this.pass();
        }
    }

    /**
     * The result of this function only is selected for participants, returning pass for spectators
     * {@link GamePlayerEvents#ACCEPT} listener.
     * @return a result
     */
    default JoinAcceptorResult ifParticipant(Function<JoinAcceptor, JoinAcceptorResult> participantFunction) {
        if (this.intent() == JoinIntent.PLAY) {
            return participantFunction.apply(this);
        } else {
            return this.pass();
        }
    }
}
