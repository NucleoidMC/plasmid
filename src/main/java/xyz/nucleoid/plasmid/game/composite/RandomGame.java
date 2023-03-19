package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        var config = context.config();

        var game = config.selectGame(Random.createLocal());
        if (game == null) {
            throw new GameOpenException(Text.translatable("text.plasmid.random.empty_composite_game_config"));
        }

        return GameOpenProcedure.withOverride(game.value().openProcedure(context.server()), game);
    }
}
