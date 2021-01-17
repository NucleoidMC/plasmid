package xyz.nucleoid.plasmid.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.*;

/**
 * Events for games being opened and closed/finishing.
 * See Javadoc of the interfaces for details about the events.
 */
public final class GameEvents {

    private GameEvents() { }

    public static final Event<OneShotGameOpening> ONE_SHOT_OPENING = EventFactory.createArrayBacked(OneShotGameOpening.class,
            listeners -> (gameId, game) -> {
        for (OneShotGameOpening listener : listeners) {
            listener.onOneShotGameOpening(gameId, game);
        }
    });

    public static final Event<GameSpaceOpened> OPENED = EventFactory.createArrayBacked(GameSpaceOpened.class,
            listeners -> (game, gameSpace) -> {
        for (GameSpaceOpened listener : listeners) {
            listener.onGameSpaceOpened(game, gameSpace);
        }
    });

    public static final Event<SetGameLogic> SET_LOGIC = EventFactory.createArrayBacked(SetGameLogic.class,
            listeners -> (gameLogic, gameSpace) -> {
        for (SetGameLogic listener : listeners) {
            listener.onSetGameLogic(gameLogic, gameSpace);
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

    public static final Event<GameSpaceClosing> CLOSING = EventFactory.createArrayBacked(GameSpaceClosing.class,
            listeners -> (gameSpace, reason) -> {
        for (GameSpaceClosing listener : listeners) {
            listener.onGameSpaceClosing(gameSpace, reason);
        }
    });

    public interface OneShotGameOpening {
        /**
         * @param gameId The game ID of the game being opened
         * @param game The game and its configuration
         */
        void onOneShotGameOpening(Identifier gameId, ConfiguredGame<?> game);
    }

    public interface GameSpaceOpened {
        /**
         * @param game The game and its configuration
         * @param gameSpace The {@link GameSpace} the game is running in.
         */
        void onGameSpaceOpened(ConfiguredGame<?> game, GameSpace gameSpace);
    }

    public interface SetGameLogic {
        /**
         * Called when the {@link GameLogic} of a {@link GameSpace} is being created/swapped (eg. when going from waiting lobby -> active game)
         * Note: This event can be called multiple times on the same {@link GameSpace}
         *
         * @param gameLogic The {@link GameLogic} that is being added to the {@link GameSpace}
         * @param gameSpace The {@link GameSpace} that is having its {@link GameLogic} changed.
         */
        void onSetGameLogic(GameLogic gameLogic, GameSpace gameSpace);
    }

    public interface GameStartRequest {
        /**
         * @param gameSpace The {@link GameSpace} the game is in
         * @param result The current {@link StartResult} of the request that may have been overridden by other listener, and may also be a failure
         * @return <code>null</code> if you don't want to override the result, or a {@link StartResult} if you want to override it.
         */
        @Nullable StartResult onRequestStart(GameSpace gameSpace, StartResult result);
    }

    public interface GameSpaceClosing {
        void onGameSpaceClosing(GameSpace gameSpace, GameCloseReason reason);
    }
}
