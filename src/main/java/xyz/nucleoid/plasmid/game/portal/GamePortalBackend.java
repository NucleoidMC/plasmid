package xyz.nucleoid.plasmid.game.portal;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.concurrent.CompletableFuture;

public interface GamePortalBackend {
    void populateDisplay(GamePortalDisplay display);

    CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player);

    interface Factory {
        GamePortalBackend create(MinecraftServer server, Identifier id);
    }
}
