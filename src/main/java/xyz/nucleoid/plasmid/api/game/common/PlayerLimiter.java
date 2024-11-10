package xyz.nucleoid.plasmid.api.game.common;

import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;

/**
 * A very simple player count limiter that games can easily apply to their {@link GameActivity}.
 * <p>
 * This implements limit for how many actively playing players can join.
 *
 * @see PlayerLimiter#addTo(GameActivity, PlayerLimiterConfig)
 * @see WaitingLobbyConfig
 */
public final class PlayerLimiter {
    private final GameSpace gameSpace;
    private final PlayerLimiterConfig config;

    private PlayerLimiter(GameSpace gameSpace, PlayerLimiterConfig config) {
        this.gameSpace = gameSpace;
        this.config = config;
    }

    /**
     * Applies this player count limiter to the given {@link GameActivity}.
     *
     * @param activity the activity to apply to
     * @param config the amount of players allowed to join the game
     */
    public static PlayerLimiter addTo(GameActivity activity, PlayerLimiterConfig config) {
        var limiter = new PlayerLimiter(activity.getGameSpace(), config);
        activity.listen(GameActivityEvents.STATE_UPDATE, limiter::updateState);
        activity.listen(GamePlayerEvents.OFFER, limiter::offerPlayer);
        return limiter;
    }

    public boolean isFull() {
        var max = this.config.maxPlayers().orElse(-1);
        if (max < 0) {
            return false;
        }
        return this.gameSpace.getPlayers().participants().size() >= max;
    }

    private GameSpaceState.Builder updateState(GameSpaceState.Builder builder) {
        var max = this.config.maxPlayers().orElse(-1);
        return builder.maxPlayers(max).canPlay(max < 0 || this.gameSpace.getPlayers().participants().size() < max).canSpectate(this.config.allowSpectators());
    }

    private JoinOfferResult offerPlayer(JoinOffer offer) {
        if (offer.intent() == JoinIntent.SPECTATE) {
            return this.config.allowSpectators() ? offer.pass() : offer.reject(GameTexts.Join.notAllowed());
        }
        var max = this.config.maxPlayers().orElse(-1);
        if (max < 0) {
            return offer.pass();
        }


        int newPlayerCount = this.gameSpace.getPlayers().participants().size() + offer.players().size();
        if (newPlayerCount > max) {
            return offer.reject(GameTexts.Join.gameFull());
        }

        return offer.pass();
    }
}
