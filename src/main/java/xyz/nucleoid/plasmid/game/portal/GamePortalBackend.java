package xyz.nucleoid.plasmid.game.portal;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.concurrent.CompletableFuture;

public interface GamePortalBackend {
    void populateDisplay(GamePortalDisplay display);

    CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player);

    default Text getName() {
        return new LiteralText("༼ つ ◕_◕ ༽つ (Unnamed)");
    }

    default int getPlayerCount() {
        return 0;
    }

    interface Factory {
        GamePortalBackend create(MinecraftServer server, Identifier id);
    }
}
