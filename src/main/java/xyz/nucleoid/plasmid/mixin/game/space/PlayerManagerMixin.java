package xyz.nucleoid.plasmid.mixin.game.space;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.RespawnResult;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.player.isolation.PlayerManagerAccess;
import xyz.nucleoid.plasmid.impl.player.isolation.PlayerResetter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    @Final
    private PlayerDataStorage playerIo;
    @Shadow
    @Final
    private Map<UUID, ServerStatsCounter> stats;
    @Shadow
    @Final
    private Map<UUID, PlayerAdvancements> advancements;

    @Shadow
    protected abstract void save(ServerPlayer player);

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private List<ServerPlayer> players;
    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

    @Shadow public abstract MinecraftServer getServer();

    @Unique
    private PlayerResetter playerResetter;

    @Inject(method = "remove", at = @At("RETURN"))
    private void removePlayer(ServerPlayer player, CallbackInfo ci) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace != null) {
            gameSpace.getPlayers().remove(player);
        }
    }

    @Redirect(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
            )
    )
    private TeleportTransition respawnPlayer(ServerPlayer player, boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace != null) {
            RespawnResult result = gameSpace.getBehavior().invoker(GamePlayerEvents.REQUEST_RESPAWN).onRequestRespawn(gameSpace, player);
            if (result instanceof RespawnResult.Respawn respawn) {
                return respawn.target();
            }
        }

        return player.findRespawnPositionAndUseSpawnBlock(consumeSpawnBlock, postTeleportTransition);
    }

    @Inject(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void respawnPlayer(
            ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> ci,
            TeleportTransition respawnTarget, ServerLevel respawnLevel, ServerPlayer respawnedPlayer
    ) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(oldPlayer);

        if (gameSpace != null) {
            if (respawnTarget.postTeleportTransition() == RespawnResult.Respawn.MARKER) {
                gameSpace.getPlayers().respawn(oldPlayer, respawnedPlayer);

                gameSpace.getBehavior().invoker(GamePlayerEvents.RESPAWN).onRespawn(oldPlayer, respawnedPlayer, alive);
            } else {
                gameSpace.getPlayers().remove(oldPlayer);

                this.plasmid$loadIntoPlayer(respawnedPlayer);
                respawnedPlayer.setServerLevel(respawnLevel);
            }
            // this is later used to apply back to the respawned player, and we want to maintain that
            var interactionManager = respawnedPlayer.gameMode;
            oldPlayer.gameMode.setGameModeForPlayer(interactionManager.getGameModeForPlayer(), interactionManager.getPreviousGameModeForPlayer());

            respawnedPlayer.updateOptions(oldPlayer.clientInformation());
        }
    }

    @Override
    public void plasmid$savePlayerData(ServerPlayer player) {
        this.save(player);
    }

    @Override
    public void plasmid$loadIntoPlayer(ServerPlayer player) {
        // Todo?
        CompoundTag userData = null;//this.getSingleplayerData();
        if (userData == null) {
            //userData = this.server.getLevelData().getLoadedPlayerTag();
        }

        ValueInput playerData;
        if (this.server.isSingleplayerOwner(player.nameAndId()) && userData != null) {
            playerData = TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), userData);
            player.load(playerData);
        } else {
            playerData = this.playerIo.load(player.nameAndId())
                    .map(compound -> TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), compound))
                    .orElse(null);
        }

        if (playerData != null) {
            player.load(playerData);
        }

        var dimension = playerData != null ? this.getDimensionFromData(playerData) : null;

        var world = this.server.getLevel(dimension);
        if (world == null) {
            world = this.server.overworld();
        }

        player.setServerLevel(world);
    }

    @Unique
    private ResourceKey<Level> getDimensionFromData(ValueInput view) {
        return view.read("Dimension", Level.RESOURCE_KEY_CODEC).orElse(Level.OVERWORLD);
    }

    @WrapWithCondition(
            method = "save",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PlayerDataStorage;save(Lnet/minecraft/world/entity/player/Player;)V")
    )
    private boolean savePlayerData(PlayerDataStorage handler, Player player) {
        return !GameSpaceManagerImpl.get().inGame(player);
    }

    @Override
    public PlayerResetter plasmid$getPlayerResetter() {
        if (this.playerResetter == null) {
            var overworld = this.server.overworld();
            var profile = new GameProfile(Util.NIL_UUID, "null");

            var player = new ServerPlayer(this.server, overworld, profile, ClientInformation.createDefault());
            this.stats.remove(Util.NIL_UUID);
            this.advancements.remove(Util.NIL_UUID);

            var tag = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.getServer().registryAccess());
            player.saveWithoutId(tag);
            tag.discard("UUID");
            tag.discard("Pos");

            this.playerResetter = new PlayerResetter(tag.buildResult());
        }

        return this.playerResetter;
    }
}
