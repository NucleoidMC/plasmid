package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GameOpenContext<C> {
    private final MinecraftServer server;
    private final GameConfig<C> game;

    public GameOpenContext(MinecraftServer server, GameConfig<C> game) {
        this.server = server;
        this.game = game;
    }

    public GameOpenProcedure open(Consumer<GameActivity> setup) {
        return gameSpace -> gameSpace.setActivity(this.game, setup);
    }

    public GameOpenProcedure openWithWorld(RuntimeWorldConfig worldConfig, BiConsumer<GameActivity, ServerWorld> setup) {
        return this.open(activity -> {
            ServerWorld world = activity.getGameSpace().addWorld(worldConfig);
            setup.accept(activity, world);
        });
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public C getConfig() {
        return this.game.getConfig();
    }

    public GameConfig<C> getGame() {
        return this.game;
    }
}
