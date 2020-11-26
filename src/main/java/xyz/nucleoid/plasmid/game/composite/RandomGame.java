package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;

import java.util.Random;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        RandomGameConfig config = context.getConfig();

        ConfiguredGame<?> game = config.selectGame(new Random());
        if (game == null) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        return game.openProcedure(context.getServer());
    }
}
