package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;

import java.util.function.Function;

public final class IsolatingPlayerTeleporter {
    private final MinecraftServer server;
    private final PlayerManagerAccess playerManager;

    public IsolatingPlayerTeleporter(MinecraftServer server) {
        this.server = server;
        this.playerManager = (PlayerManagerAccess) server.getPlayerManager();
    }

    public void teleportIn(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) {
        this.teleport(player, recreate, true);
    }

    public void teleportOut(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate) {
        this.teleport(player, recreate, false);
    }

    private void teleport(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate, boolean save) {
        PlayerManager playerManager = this.server.getPlayerManager();
        PlayerManagerAccess playerManagerAccess = (PlayerManagerAccess) playerManager;

        player.detach();
        player.setCameraEntity(player);

        if (save) {
            playerManagerAccess.plasmid$savePlayerData(player);
        }

        player.getAdvancementTracker().clearCriteria();
        this.server.getBossBarManager().onPlayerDisconnect(player);

        player.getServerWorld().removePlayer(player);
        player.removed = false;

        playerManagerAccess.plasmid$getPlayerResetter().apply(player);

        ServerWorld world = recreate.apply(player);
        player.setWorld(world);
        player.interactionManager.setWorld(world);

        WorldProperties worldProperties = world.getLevelProperties();

        ServerPlayNetworkHandler networkHandler = player.networkHandler;
        networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                world.getDimension(), world.getRegistryKey(),
                BiomeAccess.hashSeed(world.getSeed()),
                player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(),
                world.isDebugWorld(), world.isFlat(), false
        ));

        networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.abilities));
        networkHandler.sendPacket(new HeldItemChangeS2CPacket(player.inventory.selectedSlot));

        player.closeHandledScreen();

        playerManager.sendCommandTree(player);
        player.getRecipeBook().sendInitRecipesPacket(player);

        world.onPlayerTeleport(player);
        networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);

        this.server.getBossBarManager().onPlayerConnect(player);

        playerManager.sendWorldInfo(player, world);
        playerManager.sendPlayerStatus(player);

        // we just sent the full inventory, so we can consider the ScreenHandler as up-to-date
        ((ScreenHandlerAccess) player.playerScreenHandler).plasmid$resetTrackedState();

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), effect));
        }
    }

    public void teleportOutTo(ServerPlayerEntity player, ServerWorld world) {
        this.teleportOut(player, newPlayer -> {
            this.playerManager.plasmid$loadIntoPlayer(newPlayer);
            return world;
        });
    }

    public void teleportOut(ServerPlayerEntity player) {
        this.teleportOut(player, newPlayer -> {
            this.playerManager.plasmid$loadIntoPlayer(newPlayer);
            return newPlayer.getServerWorld();
        });
    }
}
