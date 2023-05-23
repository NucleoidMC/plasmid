package xyz.nucleoid.plasmid.mixin.game.space;

import io.netty.buffer.Unpooled;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.scoreboard.ServerScoreboard;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.player.isolation.PlayerManagerAccess;

import java.util.*;

import static net.minecraft.entity.Entity.RemovalReason.CHANGED_DIMENSION;

//consider splitting this into two mixins, one for player management and one for player list isolation
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements PlayerManagerAccess {
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    protected abstract void savePlayerData(ServerPlayerEntity player);
    @Final @Shadow
    private List<ServerPlayerEntity> players;
    @Final @Shadow
    private CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager;
    @Final @Shadow
    private Map<UUID, ServerPlayerEntity> playerMap;
    @Final @Shadow
    private DynamicRegistryManager.Immutable syncedRegistryManager;
    @Shadow
    private int viewDistance;
    @Shadow
    private int simulationDistance;
    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);
    @Shadow
    protected abstract void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player);
    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);
    @Shadow
    public abstract void sendPlayerStatus(ServerPlayerEntity player);
    @Shadow
    public abstract int getMaxPlayerCount();

    @Inject(method = "remove", at = @At("RETURN"))
    private void removePlayer(ServerPlayerEntity player, CallbackInfo ci) {
        var gameSpace = GameSpaceManager.get().byPlayer(player);
        if (gameSpace != null) {
            gameSpace.getPlayers().remove(player);
        }
    }

    @Inject(
            method = "respawnPlayer",
            at = @At("HEAD")
    )
    private void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        var gameSpace = GameSpaceManager.get().byPlayer(player);

        if (gameSpace == null) return;
        assert gameSpace.getWorlds().contains(player.getSpawnPointDimension()) : "Player respawned in a world that is not part of the game space, this should never happened"; //see ServerPlayNetworkHandler#respawnPlayer mixin
        gameSpace.getPlayers().kick(player);
    }

    @Override
    public void plasmid$savePlayerData(ServerPlayerEntity player) {
        this.savePlayerData(player);
    }


    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void savePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (GameSpaceManager.get().inGame(player)) {
            ci.cancel();
        }
    }

    /**
     * remove the player from the player manager and his world, it can be added again with the addPlayer method after calling entity#unsetRemoved
     */
    @Override
    public void plasmid$removePlayer(ServerPlayerEntity player, PlayerSet watcher)
    {
        this.players.remove(player); //disable the old player
        var world = player.getWorld();
        world.removePlayer(player, CHANGED_DIMENSION);
        world.getChunkManager().sendToOtherNearbyPlayers(player, new EntitiesDestroyS2CPacket(player.getId()));

        var list = new ArrayList<UUID>(watcher.size());
        watcher.forEach(i -> list.add(i.getUuid()));
        player.networkHandler.sendPacket(new PlayerRemoveS2CPacket(list));
        watcher.sendPacket(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
    }

    @Override
    public boolean plasmid$playerInstanceAlreadyExists(ServerPlayerEntity player)
    {
        return this.players.contains(player);
    }

    /**
     * send default packets and properly set the player in the player manager
     */
    @Override
    public void plasmid$AddPlayerAndSendDefaultJoinPacket(ServerPlayerEntity player, PlayerSet watchers, boolean firstSpawn)
    {
        assert !this.playerMap.containsKey(player.getUuid()) : "Player " + player + " is already added or wasn't removed from the player manager";
        //this is based on the code just after the constructor of ServerPlayNetworkHandler in the onPlayerConnect method
        var serverPlayNetworkHandler = player.networkHandler;
        var world = player.getWorld();
        var worldProperties = world.getLevelProperties();
        GameRules gameRules = world.getGameRules();
        boolean doImmediateRespawn = gameRules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean ReducedDebugInfo = gameRules.getBoolean(GameRules.REDUCED_DEBUG_INFO);

        if(firstSpawn) //I hope to use this kind of function in a direct Join system
        {
            serverPlayNetworkHandler.sendPacket(new GameJoinS2CPacket(player.getId(),
                    worldProperties.isHardcore(),
                    player.interactionManager.getGameMode(),
                    player.interactionManager.getPreviousGameMode(),
                    this.server.getWorldRegistryKeys(),
                    this.syncedRegistryManager,
                    world.getDimensionKey(), world.getRegistryKey(),
                    BiomeAccess.hashSeed(world.getSeed()),
                    this.getMaxPlayerCount(),
                    this.viewDistance,
                    this.simulationDistance,
                    ReducedDebugInfo,
                    !doImmediateRespawn,
                    world.isDebugWorld(),
                    world.isFlat(),
                    player.getLastDeathPos()));
            serverPlayNetworkHandler.sendPacket(new FeaturesS2CPacket(FeatureFlags.FEATURE_MANAGER.toId(world.getEnabledFeatures())));
            serverPlayNetworkHandler.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.server.getServerModName())));

            serverPlayNetworkHandler.sendPacket(new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values()));
            serverPlayNetworkHandler.sendPacket(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
            //serverPlayNetworkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
        }
        else
        {
            serverPlayNetworkHandler.sendPacket(new PlayerRespawnS2CPacket(player.world.getDimensionKey(), player.world.getRegistryKey(), BiomeAccess.hashSeed(player.getWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getWorld().isDebugWorld(), player.getWorld().isFlat(), (byte)1, player.getLastDeathPos()));
        }

        //originally from firstSpawn = false else statement
        serverPlayNetworkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayNetworkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.sendAbilitiesUpdate();

        this.sendCommandTree(player);
        this.sendPlayerStatus(player);
        player.getStatHandler().updateStatSet();
        player.getRecipeBook().sendInitRecipesPacket(player);
        this.sendScoreboard(world.getScoreboard(), player);

        this.server.forcePlayerSampleUpdate();

        serverPlayNetworkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        ServerMetadata serverMetadata = this.server.getServerMetadata();

        if (serverMetadata != null) {
            player.sendServerMetadata(serverMetadata);
        }

        player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(watchers.stream().toList()));

        this.players.add(player); //add player to the server
        this.playerMap.put(player.getUuid(), player);

        watchers.sendPacket(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
        this.sendWorldInfo(player, world);
        world.onPlayerConnected(player); //same as world.onRespawnPlayer or onTeleport...
        this.server.getBossBarManager().onPlayerConnect(player);

        for(var effect : player.getStatusEffects())
            serverPlayNetworkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));

        var entries = player.getDataTracker().getDirtyEntries();
        if(entries != null && !entries.isEmpty())
            watchers.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), entries));

        player.onSpawn();
        //may/must restore vehicle here, concurrently vehicle is not restored, and we can say it's a bug
    }


    /**
     * return the list of player that are in the targetPlayer's game space or players that aren't in any game space
     */
    private PlayerSet getPlayerSetFor(ServerPlayerEntity targetPlayer)
    {
        var gameSpace = GameSpaceManager.get().byPlayer(targetPlayer);
        if(gameSpace != null)
            return gameSpace.getPlayers();

        return GameSpaceManager.get().getPlayersNotInGame();
    }

    @WrapWithCondition(
            method = "savePlayerData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldSaveHandler;savePlayerData(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private boolean savePlayerData(WorldSaveHandler handler, PlayerEntity player)
    {
        return !GameSpaceManager.get().inGame(player);
    }

    /**
     * send to all player in the same game space as the targetPlayer or to all player that aren't in any game space
     */
    @Override
    public void plasmid$sendToAllFrom(Packet<?> packet, ServerPlayerEntity player)
    {
        this.getPlayerSetFor(player).sendPacket(packet);
    }


    //every redirection linked to the playerList:
    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    void sendToAllInOnPlayerConnect(PlayerManager playerManager, Packet<?> packet, ClientConnection connection, ServerPlayerEntity player)
    {
        this.plasmid$sendToAllFrom(packet, player);
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;entryFromPlayer(Ljava/util/Collection;)Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;"))
    PlayerListS2CPacket entryFromPlayer(Collection<ServerPlayerEntity> players, ClientConnection connection, ServerPlayerEntity player)
    {
        return PlayerListS2CPacket.entryFromPlayer(this.getPlayerSetFor(player).stream().toList());
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    void sendToAllInRemove(PlayerManager playerManager, Packet<?> packet, ServerPlayerEntity player)
    {
        this.plasmid$sendToAllFrom(packet, player);
    }

    @Redirect(method = "updatePlayerLatency", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    void updatePlayerLatency(PlayerManager instance, Packet<?> packet)
    {
        for(var game : GameSpaceManager.get().getOpenGameSpaces())
        {
            var playerSet = game.getPlayers();
            var playerList = playerSet.stream().toList();
            playerSet.sendPacket(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), playerList));
        }

        var set = GameSpaceManager.get().getPlayersNotInGame();
        set.sendPacket(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), set.stream().toList()));
    }

}
