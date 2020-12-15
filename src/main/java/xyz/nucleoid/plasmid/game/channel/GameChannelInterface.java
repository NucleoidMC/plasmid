package xyz.nucleoid.plasmid.game.channel;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;

public interface GameChannelInterface {
    String NBT_KEY = Plasmid.ID + ":channel";

    void setChannel(GameChannel channel);

    @Nullable
    GameChannel getChannel();

    void setDisplay(Text[] display);

    default void serializeChannel(CompoundTag root) {
        GameChannel connection = this.getChannel();
        if (connection != null) {
            root.putString(NBT_KEY, connection.getId().toString());
        }
    }

    @Nullable
    default Identifier deserializeChannelId(CompoundTag root) {
        if (root.contains(NBT_KEY, NbtType.STRING)) {
            return new Identifier(root.getString(NBT_KEY));
        }
        return null;
    }

    default void tryConnectTo(MinecraftServer server, Identifier channelId) {
        GameChannelManager channelManager = GameChannelManager.get(server);
        GameChannel channel = channelManager.byId(channelId);
        if (channel == null) {
            Plasmid.LOGGER.warn("Loaded channel endpoint with invalid channel id: '{}'", channelId);
            return;
        }

        channel.addInterface(this);
    }

    default void invalidateChannel() {
        GameChannel channel = this.getChannel();
        if (channel != null) {
            channel.removeInterface(this);
            this.setChannel(null);
        }
    }
}
