package xyz.nucleoid.plasmid.impl.portal;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.impl.Plasmid;

public interface GamePortalInterface {
    String NBT_KEY = Plasmid.id("portal").toString();

    boolean interactWithPortal(ServerPlayer player);

    void setPortal(GamePortal portal);

    @Nullable
    GamePortal getPortal();

    void setDisplay(GamePortalDisplay display);

    default void serializePortal(ValueOutput root) {
        var connection = this.getPortal();
        if (connection != null) {
            root.putString(NBT_KEY, connection.getId().toString());
        }
    }

    @Nullable
    default Identifier deserializePortalId(ValueInput root) {
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
