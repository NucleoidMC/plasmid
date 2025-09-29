package xyz.nucleoid.plasmid.mixin.game.space;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.player.isolation.PlayerManagerAccess;
import xyz.nucleoid.plasmid.impl.player.isolation.PlayerResetter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    @Final
    private PlayerSaveHandler saveHandler;
    @Shadow
    @Final
    private Map<UUID, ServerStatHandler> statisticsMap;
    @Shadow
    @Final
    private Map<UUID, PlayerAdvancementTracker> advancementTrackers;

    @Shadow
    protected abstract void savePlayerData(ServerPlayerEntity player);

    @Shadow
    public abstract NbtCompound getUserData();

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private List<ServerPlayerEntity> players;
    @Shadow @Final private Map<UUID, ServerPlayerEntity> playerMap;

    @Shadow public abstract MinecraftServer getServer();

    @Unique
    private PlayerResetter playerResetter;

    @Inject(method = "remove", at = @At("RETURN"))
    private void removePlayer(ServerPlayerEntity player, CallbackInfo ci) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace != null) {
            gameSpace.getPlayers().remove(player);
        }
    }

    @Inject(
            method = "respawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void respawnPlayer(
            ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> ci,
            TeleportTarget respawnTarget, ServerWorld respawnWorld, ServerPlayerEntity respawnedPlayer
    ) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(oldPlayer);

        if (gameSpace != null) {
            gameSpace.getPlayers().remove(oldPlayer);

            this.plasmid$loadIntoPlayer(respawnedPlayer);
            respawnedPlayer.setServerWorld(respawnWorld);

            // this is later used to apply back to the respawned player, and we want to maintain that
            var interactionManager = respawnedPlayer.interactionManager;
            oldPlayer.interactionManager.setGameMode(interactionManager.getGameMode(), interactionManager.getPreviousGameMode());

            respawnedPlayer.setClientOptions(oldPlayer.getClientOptions());
        }
    }

    @Override
    public void plasmid$savePlayerData(ServerPlayerEntity player) {
        this.savePlayerData(player);
    }

    @Override
    public void plasmid$loadIntoPlayer(ServerPlayerEntity player) {
        var userData = this.getUserData();
        if (userData == null) {
            userData = this.server.getSaveProperties().getPlayerData();
        }

        ReadView playerData;
        if (this.server.isHost(player.getPlayerConfigEntry()) && userData != null) {
            playerData = NbtReadView.create(ErrorReporter.EMPTY, player.getRegistryManager(), userData);
            player.readData(playerData);
        } else {
            playerData = this.saveHandler.loadPlayerData(player.getPlayerConfigEntry())
                    .map(compound -> NbtReadView.create(ErrorReporter.EMPTY, player.getRegistryManager(), compound))
                    .orElse(null);
        }

        if (playerData != null) {
            player.readData(playerData);
        }

        var dimension = playerData != null ? this.getDimensionFromData(playerData) : null;

        var world = this.server.getWorld(dimension);
        if (world == null) {
            world = this.server.getOverworld();
        }

        player.setServerWorld(world);
    }

    @Unique
    private RegistryKey<World> getDimensionFromData(ReadView view) {
        return view.read("Dimension", World.CODEC).orElse(World.OVERWORLD);
    }

    @WrapWithCondition(
            method = "savePlayerData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/PlayerSaveHandler;savePlayerData(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private boolean savePlayerData(PlayerSaveHandler handler, PlayerEntity player) {
        return !GameSpaceManagerImpl.get().inGame(player);
    }

    @Override
    public PlayerResetter plasmid$getPlayerResetter() {
        if (this.playerResetter == null) {
            var overworld = this.server.getOverworld();
            var profile = new GameProfile(Util.NIL_UUID, "null");

            var player = new ServerPlayerEntity(this.server, overworld, profile, SyncedClientOptions.createDefault());
            this.statisticsMap.remove(Util.NIL_UUID);
            this.advancementTrackers.remove(Util.NIL_UUID);

            var tag = NbtWriteView.create(ErrorReporter.EMPTY, this.getServer().getRegistryManager());
            player.writeData(tag);
            tag.remove("UUID");
            tag.remove("Pos");

            this.playerResetter = new PlayerResetter(tag.getNbt());
        }

        return this.playerResetter;
    }
}
