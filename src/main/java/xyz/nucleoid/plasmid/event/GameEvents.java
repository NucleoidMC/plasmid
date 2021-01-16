package xyz.nucleoid.plasmid.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.StartResult;

/**
 * Events for games being opened and closed/finishing
 */
public final class GameEvents {

    private GameEvents() { }

    public static final Event<OneShotGameOpening> ONE_SHOT_OPENING = EventFactory.createArrayBacked(OneShotGameOpening.class,
            listeners -> (gameId, game, anonymous) -> {
        for (OneShotGameOpening listener : listeners) {
            listener.onOneShotGameOpening(gameId, game, anonymous);
        }
    });

    public static final Event<GameOpening> OPENING = EventFactory.createArrayBacked(GameOpening.class,
            listeners -> game -> {
        for (GameOpening listener : listeners) {
            listener.onGameOpening(game);
        }
    });

    public static final Event<GameOpen> OPENED = EventFactory.createArrayBacked(GameOpen.class,
            listeners -> (game, gameSpace) -> {
        for (GameOpen listener : listeners) {
            listener.onGameOpen(game, gameSpace);
        }
    });

    public static final Event<GameStartRequest> START_REQUEST = EventFactory.createArrayBacked(GameStartRequest.class,
            listeners -> (gameSpace, result) -> {
        for (GameStartRequest listener : listeners) {
            StartResult tmp = listener.onRequestStart(gameSpace, result);
            if (tmp != null) result = tmp;
        }
        return result;
    });

    public static final Event<GameClosing> CLOSING = EventFactory.createArrayBacked(GameClosing.class,
            listeners -> (gameSpace, reason) -> {
        for (GameClosing listener : listeners) {
            listener.onGameClosing(gameSpace, reason);
        }
    });

    public interface GameOpening {
        /**
         * @param game The game and its configuration
         */
        void onGameOpening(ConfiguredGame<?> game);
    }

    public interface OneShotGameOpening {
        /**
         * @param gameId The game ID of the game being opened
         * @param game The game and its configuration
         * @param anonymous true if the gameId is plasmid:anonymous, false otherwise.
         */
        void onOneShotGameOpening(Identifier gameId, ConfiguredGame<?> game, boolean anonymous);
    }

    public interface GameOpen {
        /**
         * @param game The game and its configuration
         * @param gameSpace The {@link GameSpace} the game is running in.
         */
        void onGameOpen(ConfiguredGame<?> game, GameSpace gameSpace);
    }

    public interface GameStartRequest {
        /**
         * @param gameSpace The {@link GameSpace} the game is in
         * @param result The current {@link StartResult} of the request that may have been overridden by other listener, and may also be a failure
         * @return <code>null</code> if you don't want to override the result, or a {@link StartResult} if you want to override it.
         */
        @Nullable StartResult onRequestStart(GameSpace gameSpace, StartResult result);
    }

    public interface GameClosing {
        void onGameClosing(GameSpace gameSpace, GameCloseReason reason);
    }
}
