package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

/**
 * Represents a request for a {@link ServerPlayerEntity} to join a {@link GameSpace}.
 * <p>
 * This object should be used in order to construct a {@link PlayerOfferResult} object to return from a listener to the
 * {@link GamePlayerEvents#OFFER} event.
 *
 * @see GameSpace
 * @see GamePlayerEvents#OFFER
 */
public record PlayerOffer(ServerPlayerEntity player) {
    /**
     * @return the player that is requesting access to this {@link GameSpace}.
     */
    @Override
    public ServerPlayerEntity player() {
        return this.player;
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
     * @see PlayerOfferResult.Accept#and(Runnable)
     */
    public PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position) {
        return new PlayerOfferResult.Accept(world, position);
    }

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
    public PlayerOfferResult.Reject reject(Text reason) {
        return new PlayerOfferResult.Reject(reason);
    }

    /**
     * Returns an offer result that does nothing with this player offer, passing on any handling to any other listener.
     *
     * @return a "pass" offer result
     */
    public PlayerOfferResult pass() {
        return PlayerOfferResult.Pass.INSTANCE;
    }
}
