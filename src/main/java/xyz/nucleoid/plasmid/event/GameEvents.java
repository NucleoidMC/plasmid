package xyz.nucleoid.plasmid.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.activity.GameActivity;
import xyz.nucleoid.plasmid.game.config.GameConfig;

/**
 * Events for games being opened and closed/finishing.
 * See Javadoc of the interfaces for details about the events.
 */
public final class GameEvents {
    private GameEvents() {
    }

    public static final Event<GameSpaceOpened> OPENED = EventFactory.createArrayBacked(GameSpaceOpened.class, listeners -> (game, gameSpace) -> {
        for (GameSpaceOpened listener : listeners) {
            listener.onGameSpaceOpened(game, gameSpace);
        }
    });

    public static final Event<CreateActivity> CREATE_ACTIVITY = EventFactory.createArrayBacked(CreateActivity.class, listeners -> (gameSpace, activity) -> {
        for (CreateActivity listener : listeners) {
            listener.onCreateActivity(gameSpace, activity);
        }
    });

    public static final Event<DestroyActivity> DESTROY_ACTIVITY = EventFactory.createArrayBacked(DestroyActivity.class, listeners -> (gameSpace, activity, reason) -> {
        for (DestroyActivity listener : listeners) {
            listener.onDestroyActivity(gameSpace, activity, reason);
        }
    });

    public static final Event<RequestStart> START_REQUEST = EventFactory.createArrayBacked(RequestStart.class, listeners -> (gameSpace, result) -> {
        for (RequestStart listener : listeners) {
            GameResult tmp = listener.onRequestStart(gameSpace, result);
            if (tmp != null) result = tmp;
        }
        return result;
    });

    public static final Event<GameSpaceClosing> CLOSING = EventFactory.createArrayBacked(GameSpaceClosing.class, listeners -> (gameSpace, reason) -> {
        for (GameSpaceClosing listener : listeners) {
            listener.onGameSpaceClosing(gameSpace, reason);
        }
    });

    public interface GameSpaceOpened {
        /**
         * @param game The game and its configuration
         * @param gameSpace The {@link GameSpace} the game is running in.
         */
        void onGameSpaceOpened(GameConfig<?> game, GameSpace gameSpace);
    }

    public interface CreateActivity {
        /**
         * Called when the {@link GameActivity} of a {@link GameSpace} is being created (eg. when going from waiting lobby -> active game)
         * Note: This event can be called multiple times on the same {@link GameSpace}
         *
         * @param gameSpace The {@link GameSpace} that is having its {@link GameActivity} changed.
         * @param activity The {@link GameActivity} that is being added to the {@link GameSpace}
         */
        void onCreateActivity(GameSpace gameSpace, GameActivity activity);
    }

    public interface DestroyActivity {
        /**
         * Called when the {@link GameActivity} of a {@link GameSpace} is being destroyed (eg. when going from waiting lobby -> active game)
         * Note: This event can be called multiple times on the same {@link GameSpace}
         *
         * @param gameSpace The {@link GameSpace} that is having its {@link GameActivity} changed.
         * @param activity The {@link GameActivity} that is being removed from the {@link GameSpace}
         */
        void onDestroyActivity(GameSpace gameSpace, GameActivity activity, GameCloseReason reason);
    }

    public interface RequestStart {
        /**
         * @param gameSpace The {@link GameSpace} the game is in
         * @param result The current {@link GameResult} of the request that may have been overridden by other listener, and may also be a failure
         * @return <code>null</code> if you don't want to override the result, or a {@link GameResult} if you want to override it.
         */
        @Nullable
        GameResult onRequestStart(GameSpace gameSpace, @Nullable GameResult result);
    }

    public interface GameSpaceClosing {
        void onGameSpaceClosing(GameSpace gameSpace, GameCloseReason reason);
    }
}
