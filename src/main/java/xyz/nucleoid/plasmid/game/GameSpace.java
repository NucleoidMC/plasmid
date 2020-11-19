package xyz.nucleoid.plasmid.game;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents the space within which a game occurs through attached {@link GameLogic}
 */
public interface GameSpace {
    void openGame(Consumer<GameLogic> builder);

    CompletableFuture<StartResult> requestStart();

    <T extends AutoCloseable> T addResource(T resource);

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
     * Returns whether this {@link GameSpace} contains the given {@link Entity}.
     *
     * @param entity {@link Entity} to check existence of
     * @return whether the given {@link Entity} exists in this {@link GameSpace}
     */
    default boolean containsEntity(Entity entity) {
        return this.getWorld().getEntity(entity.getUuid()) != null;
    }

    /**
     * Returns the {@link ServerWorld} that this {@link GameSpace} is hosted in.
     *
     * @return the host world of this {@link GameSpace}.
     */
    ServerWorld getWorld();

    default MinecraftServer getServer() {
        return this.getWorld().getServer();
    }

    ConfiguredGame<?> getGameConfig();
}
