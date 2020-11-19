package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;

import java.util.List;
import java.util.Random;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();
        List<ConfiguredGame<?>> games = config.collectGames();

        if (games.isEmpty()) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        Random random = new Random();
        ConfiguredGame<?> game = games.get(random.nextInt(games.size()));

        return game.openProcedure(context.getServer());
    }
}
