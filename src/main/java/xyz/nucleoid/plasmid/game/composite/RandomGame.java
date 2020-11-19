package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class RandomGame {
    public static CompletableFuture<ManagedGameSpace> open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();
        List<ConfiguredGame<?>> games = config.collectGames();

        if (games.isEmpty()) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        Random random = new Random();
        ConfiguredGame<?> game = games.get(random.nextInt(games.size()));

        return game.open(context.getServer());
    }
}
