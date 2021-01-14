package xyz.nucleoid.plasmid.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.StartResult;

/**
 * Events for games being opened and closed/finishing
 */
public final class GameEvents {

    private GameEvents() { }

    public static final Event<GameOpening> OPENING = EventFactory.createArrayBacked(GameOpening.class,
            listeners -> (gameId, game, oneShot) -> {
        for (GameOpening listener : listeners) {
            listener.onGameOpening(gameId, game, oneShot);
        }
    });

    public static final Event<GameOpen> OPEN = EventFactory.createArrayBacked(GameOpen.class,
            listeners -> (gameId, game, oneShot, gameSpace) -> {
        for (GameOpen listener : listeners) {
            listener.onGameOpen(gameId, game, oneShot, gameSpace);
        }
    });

    public static final Event<GameStartRequest> START_REQUEST = EventFactory.createArrayBacked(GameStartRequest.class,
            listeners -> (gameSpace, result) -> {
        for (GameStartRequest listener : listeners) {
            listener.onRequestStart(gameSpace, result);
        }
    });

    public static final Event<GameClosing> CLOSING = EventFactory.createArrayBacked(GameClosing.class,
            listeners -> (gameSpace, reason) -> {
        for (GameClosing listener : listeners) {
            listener.onGameClosing(gameSpace, reason);
        }
    });

    public interface GameOpening {
        /**
         * @param gameId The ID of the game config being opened
         * @param game The game and its configuration
         * @param oneShot If the game is a one off, or whether it is part of a channel
         */
        void onGameOpening(Identifier gameId, ConfiguredGame<?> game, boolean oneShot);
    }

    public interface GameOpen {
        /**
         * @param gameId THe ID of the game config that has been opened
         * @param game The game and its configuration
         * @param oneShot true if the game is a one off, false if it is part of a channel
         * @param gameSpace The {@link GameSpace} the game is running in.
         */
        void onGameOpen(Identifier gameId, ConfiguredGame<?> game, boolean oneShot, GameSpace gameSpace);
    }

    public interface GameStartRequest {
        void onRequestStart(GameSpace gameSpace, StartResult result);
    }

    public interface GameClosing {
        void onGameClosing(GameSpace gameSpace, GameCloseReason reason);
    }
}
