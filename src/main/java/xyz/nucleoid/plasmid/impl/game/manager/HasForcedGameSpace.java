package xyz.nucleoid.plasmid.impl.game.manager;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.api.game.GameSpace;

public interface HasForcedGameSpace {
    GameSpace getForcedGameSpace();

    void setForcedGameSpace(GameSpace gameSpace);

    static boolean hasForcedGameSpace(MinecraftServer server) {
        return ((HasForcedGameSpace) server).getForcedGameSpace() != null;
    }
}
