package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

import java.util.concurrent.CompletableFuture;

public final class GameOpenContext<C> {
    private final MinecraftServer server;
    private final ConfiguredGame<C> game;

    GameOpenContext(MinecraftServer server, ConfiguredGame<C> game) {
        this.server = server;
        this.game = game;
    }

    public CompletableFuture<ManagedGameSpace> openWorld(BubbleWorldConfig config) {
        return ManagedGameSpace.open(this.server, this.game, config);
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public C getConfig() {
        return this.game.getConfig();
    }
}
