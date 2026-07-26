package xyz.nucleoid.plasmid.impl.player.isolation;

import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.BiomeManager;
import xyz.nucleoid.plasmid.api.game.GameSpace;

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
public final class IsolatingPlayerTeleporter {
    private final MinecraftServer server;

    public IsolatingPlayerTeleporter(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Teleports a player into a {@link GameSpace}. The player will save any associated data before teleporting.
     *
     * @param player the player to teleport
     * @param recreate a function describing how the new teleported player should be initialized
     */
    public void teleportIn(ServerPlayer player, Function<ServerPlayer, ServerLevel> recreate) {
        this.teleport(player, recreate, true);
    }

    /**
     * Teleports a player out of a {@link GameSpace}. The player will NOT save any associated data before teleporting,
     * and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     * @param recreate a function describing how the new teleported player should be initialized
     */
    public void teleportOut(ServerPlayer player, Function<ServerPlayer, ServerLevel> recreate) {
        this.teleport(player, recreate, false);
    }

    /**
     * Teleports a player out of a {@link GameSpace} and into the passed world. The player will NOT save any associated
     * data before teleporting, and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     * @param world the world to teleport to
     */
    public void teleportOutTo(ServerPlayer player, ServerLevel world) {
        this.teleportOut(player, newPlayer -> world);
    }

    /**
     * Teleports a player out of a {@link GameSpace} and into the previous world that they were apart of. The player
     * will NOT save any associated data before teleporting, and instead will restore any previously saved data.
     *
     * @param player the player to teleport
     */
    public void teleportOut(ServerPlayer player) {
        this.teleportOut(player, ServerPlayer::level);
    }

    private void teleport(ServerPlayer player, Function<ServerPlayer, ServerLevel> recreate, boolean in) {
        var playerManager = this.server.getPlayerList();
        var playerManagerAccess = (PlayerManagerAccess) playerManager;

        player.unRide();
        player.setCamera(player);

        if (in) {
            playerManagerAccess.plasmid$savePlayerData(player);
        }

        player.getAdvancements().clearTriggers();
        this.server.getCustomBossEvents().onPlayerDisconnect(player);

        player.level().removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        player.unsetRemoved();

        playerManagerAccess.plasmid$getPlayerResetter().apply(player);

        if (!in) {
            playerManagerAccess.plasmid$loadIntoPlayer(player);
        }

        player.level().getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, player));

        var world = recreate.apply(player);
        player.setServerLevel(world);

        var worldProperties = world.getLevelData();

        var spawnInfo = new CommonPlayerSpawnInfo(
            world.dimensionTypeRegistration(), world.dimension(),
            BiomeManager.obfuscateSeed(world.getSeed()),
            player.gameMode.getGameModeForPlayer(), player.gameMode.getPreviousGameModeForPlayer(),
            world.isDebug(), world.isFlat(), player.getLastDeathLocation(), player.getPortalCooldown(),
            world.getSeaLevel()
        );

        var networkHandler = player.connection;
        networkHandler.send(new ClientboundRespawnPacket(spawnInfo, ClientboundRespawnPacket.KEEP_ALL_DATA));

        player.closeContainer();

        BlockMapper.resetMapper(player);

        networkHandler.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        networkHandler.resetPosition();
        world.addDuringTeleport(player);
        networkHandler.send(new ClientboundChangeDifficultyPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        networkHandler.send(new ClientboundSetHeldSlotPacket(player.getInventory().getSelectedSlot()));
        player.onUpdateAbilities();
        playerManager.sendPlayerPermissionLevel(player);
        player.getRecipeBook().sendInitialRecipeBook(player);

        this.server.getCustomBossEvents().onPlayerConnect(player);

        playerManager.sendLevelInfo(player, world);
        playerManager.sendAllPlayerInfo(player);
        playerManager.sendActivePlayerEffects(player);

        // we just sent the full inventory, so we can consider the ScreenHandler as up-to-date
        ((ScreenHandlerAccess) player.inventoryMenu).plasmid$resetTrackedState();
    }
}
