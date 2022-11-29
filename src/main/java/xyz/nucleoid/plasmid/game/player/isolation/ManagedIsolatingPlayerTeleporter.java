package xyz.nucleoid.plasmid.game.player.isolation;

import eu.pb4.polymer.api.x.BlockMapper;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.source.BiomeAccess;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.function.Function;

public class ManagedIsolatingPlayerTeleporter implements IsolatingPlayerTeleporter{
    private final MinecraftServer server;

    public ManagedIsolatingPlayerTeleporter(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void teleportIn(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) {
        this.teleport(player, recreate, true);
    }

    @Override
    public void teleportOut(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) {
        this.teleport(player, recreate, false);
    }

    @Override
    public void teleportOutTo(ServerPlayerEntity player, ServerWorld world) {
        this.teleportOut(player, newPlayer -> world);
    }

    @Override
    public void teleportOut(ServerPlayerEntity player) {
        this.teleportOut(player, ServerPlayerEntity::getWorld);
    }

    private void teleport(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate, boolean in) {
        var playerManager = this.server.getPlayerManager();
        var playerManagerAccess = (PlayerManagerAccess) playerManager;

        player.detach();
        player.setCameraEntity(player);

        if (in) {
            playerManagerAccess.plasmid$savePlayerData(player);
        }

        player.getAdvancementTracker().clearCriteria();
        this.server.getBossBarManager().onPlayerDisconnect(player);

        player.getWorld().removePlayer(player, Entity.RemovalReason.CHANGED_DIMENSION);
        player.unsetRemoved();

        playerManagerAccess.plasmid$getPlayerResetter().apply(player);

        if (!in) {
            playerManagerAccess.plasmid$loadIntoPlayer(player);
        }

        var world = recreate.apply(player);
        player.setWorld(world);

        var worldProperties = world.getLevelProperties();

        var networkHandler = player.networkHandler;
        networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                world.getDimensionKey(), world.getRegistryKey(),
                BiomeAccess.hashSeed(world.getSeed()),
                player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(),
                world.isDebugWorld(), world.isFlat(), false,
                player.getLastDeathPos()
        ));

        networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));

        player.closeHandledScreen();

        playerManager.sendCommandTree(player);
        player.getRecipeBook().sendInitRecipesPacket(player);
        BlockMapper.resetMapper(player);

        world.onPlayerTeleport(player);
        networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

        this.server.getBossBarManager().onPlayerConnect(player);

        playerManager.sendWorldInfo(player, world);
        playerManager.sendPlayerStatus(player);

        // we just sent the full inventory, so we can consider the ScreenHandler as up-to-date
        ((ScreenHandlerAccess) player.playerScreenHandler).plasmid$resetTrackedState();

        for (var effect : player.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
        }
    }
}
