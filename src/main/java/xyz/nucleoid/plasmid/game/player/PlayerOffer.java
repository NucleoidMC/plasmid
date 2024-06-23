package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.UUID;

/**
 * Represents a request for a {@link ServerPlayerEntity} to join a {@link GameSpace}.
 * <p>
 * This object should be used in order to construct a {@link PlayerOfferResult} object to return from a listener to the
 * {@link GamePlayerEvents#OFFER} event.
 *
 * @see GameSpace
 * @see GamePlayerEvents#OFFER
 */
public interface PlayerOffer {
    /**
     * @return the {@link GameProfile} of the player that is requesting access to this {@link GameSpace}
     */
    GameProfile profile();

    /**
     * @return the {@link UUID profile UUID} of the player that is requesting access to this {@link GameSpace}
     */
    default UUID playerId() {
        return this.profile().getId();
    }

    /**
     * @return the username of the player that is requesting access to this {@link GameSpace}
     */
    default String playerName() {
        return this.profile().getName();
    }

    /**
     * Returns an offer result that accepts this player offer and allows the player into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param world the world that the player should be teleported to when accepted
     * @param position the position that the player should be teleported to when accepted
     * @return an "accept" offer result
     * @see PlayerOfferResult.Accept#thenRun(java.util.function.Consumer)
     */
    PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position);

    /**
     * Returns an offer result that rejects this player offer and does not allow the player into this {@link GameSpace}.
     * <p>
     * This function does not do anything on its own, but its result must be returned within a
     * {@link GamePlayerEvents#OFFER} listener.
     *
     * @param reason a text message that explains why this player was rejected
     * @return a "reject" offer result
     * @see GameTexts.Join
     */
    PlayerOfferResult.Reject reject(Text reason);

    /**
     * Returns an offer result that does nothing with this player offer, passing on any handling to any other listener.
     *
     * @return a "passing" offer result
     */
    default PlayerOfferResult pass() {
        return PlayerOfferResult.PASS;
    }
}
