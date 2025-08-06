package xyz.nucleoid.plasmid.impl.portal;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.impl.Plasmid;

public interface GamePortalInterface {
    String NBT_KEY = Plasmid.id("portal").toString();

    boolean interactWithPortal(ServerPlayerEntity player);

    void setPortal(GamePortal portal);

    @Nullable
    GamePortal getPortal();

    void setDisplay(GamePortalDisplay display);

    default void serializePortal(WriteView root) {
        var connection = this.getPortal();
        if (connection != null) {
            root.putString(NBT_KEY, connection.getId().toString());
        }
    }

    @Nullable
    default Identifier deserializePortalId(ReadView root) {
        return root.read(NBT_KEY, Identifier.CODEC).orElse(null);
    }

    default boolean tryConnectTo(Identifier portalId) {
        var portal = GamePortalManager.INSTANCE.byId(portalId);
        if (portal == null) {
            //Plasmid.LOGGER.warn("Loaded channel endpoint with invalid portal id: '{}'", portalId);
            return false;
        }

        portal.addInterface(this);
        return true;
    }

    default void invalidatePortal() {
        var portal = this.getPortal();
        if (portal != null) {
            portal.removeInterface(this);
            this.setPortal(null);
        }
    }

    default boolean updatePortalImmediately() {
        return true;
    };
}
