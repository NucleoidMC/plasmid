package xyz.nucleoid.plasmid.game.channel;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.channel.oneshot.OneshotChannelSystem;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class GameChannelManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":game_channels";

    private final ConfiguredChannelSystem configuredChannels;
    private final OneshotChannelSystem oneshotChannels;

    private final GameChannelSystem[] systems;

    GameChannelManager(MinecraftServer server) {
        super(KEY);
        this.configuredChannels = ConfiguredChannelSystem.INSTANCE;
        this.oneshotChannels = new OneshotChannelSystem(server);
        this.systems = new GameChannelSystem[] { this.configuredChannels, this.oneshotChannels };
    }

    public static GameChannelManager get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(() -> new GameChannelManager(server), KEY);
    }

    public CompletableFuture<GameChannel> openOneshot(Identifier gameId, ConfiguredGame<?> game) {
        return this.oneshotChannels.open(gameId, game);
    }

    // TODO: In the future, we need to associate each player with a channel to force exclusivity
    @Nullable
    public GameChannel getChannelFor(ServerPlayerEntity player) {
        for (GameChannel channel : this.getChannels()) {
            if (channel.containsPlayer(player)) {
                return channel;
            }
        }
        return null;
    }

    @Nullable
    public GameChannel byId(Identifier id) {
        for (GameChannelSystem system : this.systems) {
            GameChannel channel = system.byId(id);
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    public Set<Identifier> keySet() {
        Set<Identifier> keys = Collections.emptySet();
        for (GameChannelSystem system : this.systems) {
            keys = Sets.union(keys, system.keySet());
        }
        return keys;
    }

    public Collection<GameChannel> getChannels() {
        List<GameChannel> channels = new ArrayList<>();
        for (GameChannelSystem system : this.systems) {
            channels.addAll(system.getChannels());
        }
        return channels;
    }

    @Override
    public CompoundTag toTag(CompoundTag root) {
        return root;
    }

    @Override
    public void fromTag(CompoundTag root) {
    }
}
