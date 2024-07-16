package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a request for of a player or group of players to join a {@link GameSpace}.
 * <p>
 * This object should be used in order to construct a {@link PlayerOfferResult} object to return from a listener to the
 * {@link GamePlayerEvents#OFFER} event.
 *
 * @see GameSpace
 * @see GamePlayerEvents#OFFER
 */
public interface PlayerOffer {
    /**
     * @return the set of {@link GameProfile} of the players that are requesting access to this {@link GameSpace}
     */
    Collection<GameProfile> players();

    /**
     * @return the {@link UUID profile UUID} of the players that are requesting access to this {@link GameSpace}
     */
    default Collection<UUID> playerIds() {
        return this.players()
                .stream()
                .map(GameProfile::getId)
                .toList();
    }

    /**
     * @return the usernames of the players that are requesting access to this {@link GameSpace}
     */
    default Collection<String> playerNames() {
        return this.players()
                .stream()
                .map(GameProfile::getName)
                .toList();
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
     * @see PlayerOfferResult.Accept#thenRun(Consumer)
     */
    PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position, float yaw, float pitch);

    /**
     * Returns an offer result that accepts this offer and allows the player into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param world the world that the players should be teleported to when accepted
     * @param position the position that the players should be teleported to when accepted
     * @return an "accept" offer result
     * @see PlayerOfferResult.Accept#thenRun(Consumer)
     */
    default PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position) {
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
     * @see PlayerOfferResult.Accept#thenRun(Consumer)
     */
    default PlayerOfferResult.Accept accept(ServerWorld world) {
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
    PlayerOfferResult.Reject reject(Text reason);

    /**
     * Returns an offer result that does nothing with this offer, passing on any handling to any other listener.
     *
     * @return a "passing" offer result
     */
    default PlayerOfferResult pass() {
        return PlayerOfferResult.PASS;
    }
}
