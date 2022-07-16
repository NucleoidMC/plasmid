package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;

import java.util.Random;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        var config = context.config();

        var game = config.selectGame(new Random());
        if (game == null) {
            throw new GameOpenException(Text.translatable("text.plasmid.random.empty_composite_game_config"));
        }

        return game.openProcedure(context.server());
    }
}
