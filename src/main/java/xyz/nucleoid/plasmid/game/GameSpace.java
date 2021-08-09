package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
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
public interface GameSpace {
    /**
     * @return the host server of this {@link GameSpace}
     */
    MinecraftServer getServer();

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
     * Creates and adds a temporary world to be associated with this {@link GameSpace}.
     * When the game is closed, the world will be deleted.
     *
     * @param worldConfig a config describing how the new world should be created
     * @return the created world instance
     * @see RuntimeWorldConfig
     */
    ServerWorld addWorld(RuntimeWorldConfig worldConfig);

    /**
     * Removes and deletes a temporary world that is associated with this {@link GameSpace}.
     * The passed world must have been created through {@link GameSpace#addWorld(RuntimeWorldConfig)}.
     *
     * @param world the world instance to delete
     * @see GameSpace#addWorld(RuntimeWorldConfig)
     */
    void removeWorld(ServerWorld world);

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
     * Screens a group of players and returns whether the collective group should be allowed into the game.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#SCREEN_JOINS}.
     *
     * @param players the group of players trying to join
     * @return a {@link GameResult} describing whether this group can join this game, or an error if not
     * @see GamePlayerEvents#SCREEN_JOINS
     * @see GameSpace#offerPlayer(ServerPlayerEntity)
     * @see xyz.nucleoid.plasmid.game.player.GamePlayerJoiner
     */
    GameResult screenPlayerJoins(Collection<ServerPlayerEntity> players);

    /**
     * Offers an individual player to join this game. If accepted, they will be teleported into the game, and if not
     * an error {@link GameResult} will be returned.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#OFFER}.
     *
     * @param player the player trying to join
     * @return a {@link GameResult} describing whether this player joined the game, or an error if not
     * @see GamePlayerEvents#OFFER
     * @see xyz.nucleoid.plasmid.game.player.GamePlayerJoiner
     */
    GameResult offerPlayer(ServerPlayerEntity player);

    /**
     * Attempts to remove the given {@link ServerPlayerEntity} from this {@link GameSpace}.
     * When a player is removed, they will be teleported back to their former location prior to joining.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link GameSpace}
     * @return whether the {@link ServerPlayerEntity} was successfully removed
     */
    boolean kickPlayer(ServerPlayerEntity player);

    /**
     * Returns all {@link ServerPlayerEntity}s in this {@link GameSpace}.
     *
     * <p>{@link GameSpace#containsPlayer(ServerPlayerEntity)} can be used to check if a {@link ServerPlayerEntity} is in this {@link GameSpace} instead.
     *
     * @return a {@link PlayerSet} that contains all {@link ServerPlayerEntity}s in this {@link GameSpace}
     */
    PlayerSet getPlayers();

    /**
     * @return the number of players in this {@link GameSpace}.
     */
    default int getPlayerCount() {
        return this.getPlayers().size();
    }

    /**
     * Returns whether this {@link GameSpace} contains the given {@link ServerPlayerEntity}.
     *
     * @param player {@link ServerPlayerEntity} to check existence of
     * @return whether the given {@link ServerPlayerEntity} exists in this {@link GameSpace}
     */
    default boolean containsPlayer(ServerPlayerEntity player) {
        return this.getPlayers().contains(player);
    }

    /**
     * @return the lifecycle manager for this {@link GameSpace}
     */
    GameLifecycle getLifecycle();

    /**
     * @return the {@link GameConfig} that was responsible for creating this {@link GameSpace}
     */
    GameConfig<?> getSourceConfig();

    /**
     * @return the globally unique ID for this {@link GameSpace}
     */
    UUID getId();

    /**
     * Returns the ID assigned to this {@link GameSpace} instance that can be referenced by players in commands.
     * This ID is not guaranteed to be unique over time, but only unique during the existence of this {@link GameSpace}!
     *
     * @return the user-referencable ID for this {@link GameSpace}
     */
    Identifier getUserId();

    /**
     * @return the number of ticks that have passed since this {@link GameSpace} was created
     */
    long getTime();

    /**
     * Note: bundle namespaces can only contain the characters a-zA-Z0-9_
     *
     * @param namespace The statistic namespace to get a bundle for
     * @return the {@link GameStatisticBundle} for the given namespace
     */
    GameStatisticBundle getStatistics(String namespace);

    /**
     * @param consumer Will be called for every non-empty {@link GameStatisticBundle} in this {@link GameSpace}
     */
    void visitAllStatistics(BiConsumer<String, GameStatisticBundle> consumer);
}
