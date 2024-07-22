package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.Set;
import java.util.UUID;
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
     * @return an "accept" offer result
     */
    default JoinOfferResult.Accept accept() {
        return JoinOfferResult.ACCEPT;
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
