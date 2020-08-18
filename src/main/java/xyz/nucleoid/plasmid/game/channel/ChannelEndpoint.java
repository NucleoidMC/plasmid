package xyz.nucleoid.plasmid.game.channel;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;

import javax.annotation.Nullable;

public interface ChannelEndpoint {
    String NBT_KEY = Plasmid.ID + ":channel";

    void setConnection(GameChannel connection);

    @Nullable
    GameChannel getConnection();

    void updateDisplay(GameChannelDisplay display);

    default void serializeConnection(CompoundTag root) {
        GameChannel connection = this.getConnection();
        if (connection != null) {
            root.putString(NBT_KEY, connection.getId().toString());
        }
    }

    @Nullable
    default Identifier deserializeConnectionId(CompoundTag root) {
        if (root.contains(NBT_KEY, NbtType.STRING)) {
            return new Identifier(root.getString(NBT_KEY));
        }
        return null;
    }

    default void tryConnectTo(MinecraftServer server, Identifier channelId) {
        GameChannelManager channelManager = GameChannelManager.get(server);
        GameChannel channel = channelManager.get(channelId);
        if (channel == null) {
            Plasmid.LOGGER.warn("Loaded channel endpoint with invalid channel id: '{}'", channelId);
            return;
        }

        channel.connectTo(this);
    }

    default void invalidateConnection() {
        GameChannel connection = this.getConnection();
        if (connection != null) {
            connection.removeConnection(this);
            this.setConnection(null);
        }
    }
}
