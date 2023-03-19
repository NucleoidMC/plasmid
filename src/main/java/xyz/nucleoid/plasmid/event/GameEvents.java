package xyz.nucleoid.plasmid.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.Set;

/**
 * Events for games being opened and closed/finishing.
 * See Javadoc of the interfaces for details about the events.
 */
public final class GameEvents {
    private GameEvents() {
    }

    public static final Event<GameSpaceOpened> OPENED = EventFactory.createArrayBacked(GameSpaceOpened.class, listeners -> (game, gameSpace) -> {
        for (var listener : listeners) {
            listener.onGameSpaceOpened(game, gameSpace);
        }
    });

    public static final Event<CreateActivity> CREATE_ACTIVITY = EventFactory.createArrayBacked(CreateActivity.class, listeners -> (gameSpace, activity) -> {
        for (var listener : listeners) {
            listener.onCreateActivity(gameSpace, activity);
        }
    });

    public static final Event<DestroyActivity> DESTROY_ACTIVITY = EventFactory.createArrayBacked(DestroyActivity.class, listeners -> (gameSpace, activity, reason) -> {
        for (var listener : listeners) {
            listener.onDestroyActivity(gameSpace, activity, reason);
        }
    });

    public static final Event<RequestStart> START_REQUEST = EventFactory.createArrayBacked(RequestStart.class, listeners -> (gameSpace, result) -> {
        for (var listener : listeners) {
            var tmp = listener.onRequestStart(gameSpace, result);
            if (tmp != null) result = tmp;
        }
        return result;
    });

    public static final Event<GameSpaceClosing> CLOSING = EventFactory.createArrayBacked(GameSpaceClosing.class, listeners -> (gameSpace, reason) -> {
        for (var listener : listeners) {
            listener.onGameSpaceClosing(gameSpace, reason);
        }
    });

    public static final Event<CollectPlayersForJoin> COLLECT_PLAYERS_FOR_JOIN = EventFactory.createArrayBacked(CollectPlayersForJoin.class, listeners -> (gameSpace, player, additional) -> {
        for (var listener : listeners) {
            listener.collectPlayersForJoin(gameSpace, player, additional);
        }
    });

    public static final Event<PlayerJoin> PLAYER_JOIN = EventFactory.createArrayBacked(PlayerJoin.class, listeners -> (gameSpace, player) -> {
        for (var listener : listeners) {
            listener.onPlayerJoin(gameSpace, player);
        }
    });

    public static final Event<PlayerLeft> PLAYER_LEFT = EventFactory.createArrayBacked(PlayerLeft.class, listeners -> (gameSpace, player) -> {
        for (var listener : listeners) {
            listener.onPlayerLeft(gameSpace, player);
        }
    });

    public interface GameSpaceOpened {
        /**
         * @param game The game and its configuration
         * @param gameSpace The {@link GameSpace} the game is running in.
         */
        void onGameSpaceOpened(RegistryEntry<GameConfig<?>> game, GameSpace gameSpace);
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

    public interface CollectPlayersForJoin {
        /**
         * Called when a {@link ServerPlayerEntity} tries to join a {@link GameSpace}. This event is responsible for
         * collecting any additional players who should attempt to join along with the initial player.
         *
         * @param gameSpace the {@link GameSpace} being joined
         * @param player the initial player who tried to join a {@link GameSpace}
         * @param additional a set of additional players who should join the game
         */
        void collectPlayersForJoin(GameSpace gameSpace, ServerPlayerEntity player, Set<ServerPlayerEntity> additional);
    }


    public interface PlayerJoin {
        /**
         * @param gameSpace The {@link GameSpace} the game is running in.
         * @param player the initial player who tried to join a {@link GameSpace}
         */
        void onPlayerJoin(GameSpace gameSpace, ServerPlayerEntity player);
    }

    public interface PlayerLeft {
        /**
         * @param gameSpace The {@link GameSpace} the game is running in.
         * @param player the initial player who tried to join a {@link GameSpace}
         */
        void onPlayerLeft(GameSpace gameSpace, ServerPlayerEntity player);
    }

}
