package xyz.nucleoid.plasmid.mixin.game.space;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.isolation.PlayerManagerAccess;
import xyz.nucleoid.plasmid.game.player.isolation.PlayerResetter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    @Final
    private WorldSaveHandler saveHandler;
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

    @Unique
    private PlayerResetter playerResetter;

    @Inject(method = "remove", at = @At("RETURN"))
    private void removePlayer(ServerPlayerEntity player, CallbackInfo ci) {
        ManagedGameSpace gameSpace = GameSpaceManager.get().byPlayer(player);
        if (gameSpace != null) {
            gameSpace.removePlayer(player);
        }
    }

    @Inject(
            method = "respawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.PRINT
    )
    private void respawnPlayer(
            ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> ci,
            BlockPos spawnPos, float spawnAngle, boolean spawnSet, ServerWorld spawnWorld, Optional<Vec3d> respawnPoint,
            ServerWorld respawnWorld, ServerPlayerEntity respawnedPlayer
    ) {
        ManagedGameSpace gameSpace = GameSpaceManager.get().byPlayer(oldPlayer);

        if (gameSpace != null) {
            gameSpace.removePlayer(oldPlayer);

            this.plasmid$loadIntoPlayer(respawnedPlayer);
            respawnedPlayer.setWorld(respawnWorld);

            // this is later used to apply back to the respawned player, and we want to maintain that
            var interactionManager = respawnedPlayer.interactionManager;
            oldPlayer.interactionManager.setGameMode(interactionManager.getGameMode(), interactionManager.getPreviousGameMode());
        }
    }

    @Override
    public void plasmid$savePlayerData(ServerPlayerEntity player) {
        this.savePlayerData(player);
    }

    @Override
    public void plasmid$loadIntoPlayer(ServerPlayerEntity player) {
        NbtCompound userData = this.getUserData();
        if (userData == null) {
            userData = this.server.getSaveProperties().getPlayerData();
        }

        NbtCompound playerData;
        if (player.getName().getString().equals(this.server.getUserName()) && userData != null) {
            playerData = userData;
            player.readNbt(userData);
        } else {
            playerData = this.saveHandler.loadPlayerData(player);
        }

        RegistryKey<World> dimension = playerData != null ? this.getDimensionFromData(playerData) : null;

        ServerWorld world = this.server.getWorld(dimension);
        if (world == null) {
            world = this.server.getOverworld();
        }

        player.setWorld(world);
        player.interactionManager.setWorld(world);

        player.setGameMode(playerData);
    }

    @Unique
    private RegistryKey<World> getDimensionFromData(NbtCompound playerData) {
        return DimensionType.worldFromDimensionNbt(new Dynamic<>(NbtOps.INSTANCE, playerData.get("Dimension")))
                .resultOrPartial(LOGGER::error)
                .orElse(World.OVERWORLD);
    }

    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void savePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (GameSpaceManager.get().inGame(player)) {
            ci.cancel();
        }
    }

    @Override
    public PlayerResetter plasmid$getPlayerResetter() {
        if (this.playerResetter == null) {
            ServerWorld overworld = this.server.getOverworld();
            GameProfile profile = new GameProfile(Util.NIL_UUID, "null");

            ServerPlayerEntity player = new ServerPlayerEntity(this.server, overworld, profile);
            this.statisticsMap.remove(Util.NIL_UUID);
            this.advancementTrackers.remove(Util.NIL_UUID);

            NbtCompound tag = new NbtCompound();
            player.writeNbt(tag);
            tag.remove("UUID");
            tag.remove("Pos");

            this.playerResetter = new PlayerResetter(tag);
        }

        return this.playerResetter;
    }
}
