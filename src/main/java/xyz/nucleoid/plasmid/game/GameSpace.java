package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Collection;
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

    void setActivity(GameConfig<?> config, Consumer<GameActivity> builder);

    /**
     * Closes this {@link GameSpace} with the given reason.
     * The associated {@link GameActivity} is closed and all players will be removed.
     *
     * @param reason the reason for this game closing
     */
    void close(GameCloseReason reason);

    ServerWorld addWorld(RuntimeWorldConfig worldConfig);

    void removeWorld(ServerWorld world);

    GameResult requestStart();

    GameResult screenPlayerJoins(Collection<ServerPlayerEntity> players);

    GameResult offerPlayer(ServerPlayerEntity player);

    /**
     * Attempts to remove the given {@link ServerPlayerEntity} from this {@link GameSpace}.
     * When a player is removed, they will be teleported back to their former location prior to joining
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

    GameConfig<?> getSourceConfig();

    Identifier getId();

    long getTime();
}
