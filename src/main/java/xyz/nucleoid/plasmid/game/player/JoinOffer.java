package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a request for of a player or group of players to join a {@link GameSpace}.
 * <p>
 * This object should be used in order to construct a {@link JoinOfferResult} object to return from a listener to the
 * {@link GamePlayerEvents#OFFER} event.
 *
 * @see GameSpace
 * @see GamePlayerEvents#OFFER
 */
public interface JoinOffer {
    /**
     * @return the set of {@link GameProfile} of the players that are requesting access to this {@link GameSpace}
     */
    Set<GameProfile> players();

    /**
     * @return the {@link UUID profile UUID} of the players that are requesting access to this {@link GameSpace}
     */
    default Set<UUID> playerIds() {
        return this.players()
                .stream()
                .map(GameProfile::getId)
                .collect(Collectors.toSet());
    }

    /**
     * @return the usernames of the players that are requesting access to this {@link GameSpace}
     */
    default Set<String> playerNames() {
        return this.players()
                .stream()
                .map(GameProfile::getName)
                .collect(Collectors.toSet());
    }

    /**
     * @return the {@link JoinIntent 'intent'} of the players, such as whether they want to participate or spectate
     * @see JoinIntent
     */
    JoinIntent intent();

    /**
     * Returns an offer result that accepts this offer and allows the players into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param world the world that the players should be teleported to when accepted
     * @param position the position that the players should be teleported to when accepted
     * @param yaw the 'yaw' angle that the players should be teleported to when accepted
     * @param pitch the 'pitch' angle that the players should be teleported to when accepted
     * @return an "accept" offer result
     * @see JoinOfferResult.Accept#thenRun(Consumer)
     */
    JoinOfferResult.Accept accept(ServerWorld world, Vec3d position, float yaw, float pitch);

    /**
     * Returns an offer result that accepts this offer and allows the player into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param world the world that the players should be teleported to when accepted
     * @param position the position that the players should be teleported to when accepted
     * @return an "accept" offer result
     * @see JoinOfferResult.Accept#thenRun(Consumer)
     */
    default JoinOfferResult.Accept accept(ServerWorld world, Vec3d position) {
        return this.accept(world, position, 0.0f, 0.0f);
    }

    /**
     * Returns an offer result that accepts this offer and allows the players into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param world the world that the players should be teleported to when accepted
     * @return an "accept" offer result
     * @see JoinOfferResult.Accept#thenRun(Consumer)
     */
    default JoinOfferResult.Accept accept(ServerWorld world) {
        return this.accept(world, Vec3d.ofBottomCenter(world.getSpawnPos()));
    }

    /**
     * Returns an offer result that rejects this offer and does not allow the players into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param reason a text message that explains why these players were rejected
     * @return a "reject" offer result
     * @see GameTexts.Join
     */
    JoinOfferResult.Reject reject(Text reason);

    /**
     * Returns an offer result that does nothing with this offer, passing on any handling to any other listener.
     *
     * @return a "passing" offer result
     */
    default JoinOfferResult pass() {
        return JoinOfferResult.PASS;
    }
}
