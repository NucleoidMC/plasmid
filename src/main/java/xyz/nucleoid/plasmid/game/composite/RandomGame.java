package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.Random;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        RandomGameConfig config = context.getConfig();

        GameConfig<?> game = config.selectGame(new Random());
        if (game == null) {
            throw new GameOpenException(new TranslatableText("text.plasmid.random.empty_composite_game_config"));
        }

        return game.openProcedure(context.getServer());
    }
}
