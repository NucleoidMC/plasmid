package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameSpace;
import java.util.function.Function;

/**
 * Teleports payer in and out of a {@link GameSpace}. This involves ensuring that the player does not bring anything
 * into the game space as well as to not bring anything out of the game space.
 * <p>
 * The player's NBT must be saved on entry to a game space, and it must not be saved when exiting and instead restored.
 * <p>
 * This class is also responsible for resetting player state and sending packets such that the player is fully refreshed
 * after teleporting and no weird issues can arise from invalid state passing through dimensions.
 */
public interface IsolatingPlayerTeleporter {
        /**
     * Teleports a player into a {@link GameSpace}. The player will save any associated data before teleporting.
     *
     * @param player the player to teleport
     * @param recreate a function describing how the new teleported player should be initialized
     */
    public void teleportIn(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate);

    /**
     * Teleports a player out of a {@link GameSpace}. The player will NOT save any associated data before teleporting,
     * and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     * @param recreate a function describing how the new teleported player should be initialized
     */
    public void teleportOut(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) ;

    /**
     * Teleports a player out of a {@link GameSpace} and into the passed world. The player will NOT save any associated
     * data before teleporting, and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     * @param world the world to teleport to
     */
    public void teleportOutTo(ServerPlayerEntity player, ServerWorld world);

    /**
     * Teleports a player out of a {@link GameSpace} and into the previous world that they were apart of. The player
     * will NOT save any associated data before teleporting, and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     */
    public void teleportOut(ServerPlayerEntity player);
}
