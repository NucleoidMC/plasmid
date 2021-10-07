package xyz.nucleoid.plasmid.game.portal;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;

public interface GamePortalInterface {
    String NBT_KEY = Plasmid.ID + ":portal";

    boolean interactWithPortal(ServerPlayerEntity player);

    void setPortal(GamePortal portal);

    @Nullable
    GamePortal getPortal();

    @Nullable
    Identifier getStoredPortalId();

    void setDisplay(GamePortalDisplay display);

    default void serializePortal(NbtCompound root) {
        var connection = this.getStoredPortalId();
        if (connection != null) {
            root.putString(NBT_KEY, connection.toString());
        }
    }

    @Nullable
    default Identifier deserializePortalId(NbtCompound root) {
        if (root.contains(NBT_KEY, NbtType.STRING)) {
            return new Identifier(root.getString(NBT_KEY));
        }
        return null;
    }

    default void tryConnectTo(Identifier portalId) {
        var portal = GamePortalManager.INSTANCE.byId(portalId);
        if (portal == null) {
            Plasmid.LOGGER.warn("Loaded channel endpoint with invalid portal id: '{}'", portalId);
            return;
        }

        portal.addInterface(this);
    }

    default void invalidatePortal() {
        var portal = this.getPortal();
        if (portal != null) {
            portal.removeInterface(this);
            this.setPortal(null);
        }
    }
}
