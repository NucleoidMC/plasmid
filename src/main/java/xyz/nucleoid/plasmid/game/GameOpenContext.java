package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

import java.util.function.Consumer;

public final class GameOpenContext<C> {
    private final MinecraftServer server;
    private final ConfiguredGame<C> game;

    GameOpenContext(MinecraftServer server, ConfiguredGame<C> game) {
        this.server = server;
        this.game = game;
    }

    public GameOpenProcedure createOpenProcedure(BubbleWorldConfig worldConfig, Consumer<GameLogic> configureGame) {
        return new GameOpenProcedure(this.server, this.game, this.game, worldConfig, configureGame);
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public C getConfig() {
        return this.game.getConfig();
    }

    public ConfiguredGame<C> getGame() {
        return this.game;
    }
}
