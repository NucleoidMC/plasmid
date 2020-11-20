package xyz.nucleoid.plasmid.game.channel;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import xyz.nucleoid.plasmid.Plasmid;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class GameChannelManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":game_channels";

    private final MinecraftServer server;
    private final Map<Identifier, GameChannel> channels = new HashMap<>();

    GameChannelManager(MinecraftServer server) {
        super(KEY);
        this.server = server;
    }

    public static GameChannelManager get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(() -> new GameChannelManager(server), KEY);
    }

    public boolean add(GameChannel channel) {
        Identifier id = channel.getId();
        if (!this.channels.containsKey(id)) {
            this.channels.put(id, channel);
            this.markDirty();
            return true;
        }

        return false;
    }

    public boolean remove(Identifier identifier) {
        GameChannel channel = this.channels.remove(identifier);
        if (channel != null) {
            channel.invalidate();
            this.markDirty();
            return true;
        }
        return false;
    }

    @Nullable
    public GameChannel get(Identifier identifier) {
        return this.channels.get(identifier);
    }

    public Set<Identifier> getKeys() {
        return this.channels.keySet();
    }

    @Override
    public CompoundTag toTag(CompoundTag root) {
        ListTag channelList = new ListTag();

        for (Map.Entry<Identifier, GameChannel> entry : this.channels.entrySet()) {
            Identifier channelId = entry.getKey();
            GameChannel channel = entry.getValue();

            DataResult<Tag> result = GameChannel.CODEC.encodeStart(NbtOps.INSTANCE, channel);
            result.result().ifPresent(channelList::add);

            result.error().ifPresent(error -> {
                Plasmid.LOGGER.warn("Failed to encode channel with id '{}': {}", channelId, error);
            });
        }

        root.put("channels", channelList);

        return root;
    }

    @Override
    public void fromTag(CompoundTag root) {
        this.channels.clear();

        ListTag channelList = root.getList("channels", NbtType.COMPOUND);

        for (int i = 0; i < channelList.size(); i++) {
            CompoundTag channelTag = channelList.getCompound(i);

            DataResult<GameChannel> result = GameChannel.CODEC.decode(NbtOps.INSTANCE, channelTag).map(Pair::getFirst);
            result.result().ifPresent(channel -> {
                this.channels.put(channel.getId(), channel);
            });

            result.error().ifPresent(error -> {
                Plasmid.LOGGER.warn("Failed to decode channel: {}", error);
            });
        }
    }
}
