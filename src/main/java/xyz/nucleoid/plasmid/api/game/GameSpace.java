package xyz.nucleoid.plasmid.api.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.world.GameSpaceWorlds;

import java.util.function.Consumer;

/**
 * Represents an instance of a game, and the "space" within which it occurs.
 * <p>
 * The {@link GameSpace} controls all of its attached {@link ServerWorld} objects, all joined players, and all the
 * behavior that takes place within the game.
 * <p>
 * Behavior should be controlled by game implementations through the use of {@link GameActivity} instances.
 *
 * @see GameType
 * @see GameActivity
 */
public interface GameSpace extends GameAttachmentHolder {
    /**
     * @return the host server of this {@link GameSpace}
     */
    MinecraftServer getServer();

    /**
     * @return all metadata associated with this {@link GameSpace}
     */
    GameSpaceMetadata getMetadata();

    /**
     * Sets and replaces the active {@link GameActivity} on this {@link GameSpace}.
     * <p>
     * The old activity will be closed with all its players removed. The sequence of expected events follows that as
     * described by {@link GameSpace#close(GameCloseReason)}.
     * <p>
     * After being built, the following events will be fired on the new activity in sequence:
     * <li>{@link GameActivityEvents#CREATE}</li>
     * <li>{@link GamePlayerEvents#ADD} for every player in this {@link GameSpace}</li>
     * <li>{@link GameActivityEvents#ENABLE}</li>
     *
     * @param builder a builder to set up a new activity's listeners and rules
     */
    void setActivity(Consumer<GameActivity> builder);

    /**
     * Submits a request to the currently active {@link GameActivity} for this game to be started.
     * What a start request means is dependent on the game implementation, and a game does not necessarily need to
     * respond to this event unless they wish to respond to the {@code /game start} command.
     *
     * @return a {@link GameResult} describing whether this game was successfully started, or an error if not
     * @see GameActivityEvents#REQUEST_START
     */
    GameResult requestStart();

    /**
     * Closes this {@link GameSpace} with the given reason.
     * The associated {@link GameActivity} is closed and all players will be removed.
     * <p>
     * The following events will be fired on the closed activity in sequence:
     * <li>{@link GameActivityEvents#DISABLE}</li>
     * <li>{@link GamePlayerEvents#REMOVE} for every player in this {@link GameSpace}</li>
     * <li>{@link GameActivityEvents#DESTROY}</li>
     *
     * @param reason the reason for this game closing
     */
    void close(GameCloseReason reason);

    /**
     * Returns all {@link ServerPlayerEntity}s in this {@link GameSpace}.
     *
     * <p>{@link GameSpacePlayers#contains(ServerPlayerEntity)} can be used to check if a {@link ServerPlayerEntity} is in this {@link GameSpace} instead.
     *
     * @return a {@link PlayerSet} that contains all {@link ServerPlayerEntity}s in this {@link GameSpace}
     */
    GameSpacePlayers getPlayers();

    /**
     * Returns the manager for all attached {@link ServerWorld} instances to this {@link GameSpace}.
     * <p>
     * {@link GameSpaceWorlds#add(RuntimeWorldConfig)} can be used to attach a {@link GameSpace}.
     *
     * @return the {@link ServerWorld} manager for this {@link GameSpace}
     */
    GameSpaceWorlds getWorlds();

    /**
     * @return the number of ticks that have passed since this {@link GameSpace} was created
     */
    long getTime();

    /**
     * @return the lifecycle manager for this {@link GameSpace}
     */
    GameLifecycle getLifecycle();

    /**
     * @return the statistics manager for this {@link GameSpace}
     */
    GameSpaceStatistics getStatistics();

    /**
     * @return the current state of this {@link GameSpace}. It should be used purely for non-logic information.
     */
    GameSpaceState getState();

    /**
     * @return true if this GameSpace is closed, false otherwise
     */
    boolean isClosed();
}
