package xyz.nucleoid.plasmid.impl.game.composite;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        var config = context.config();

        var game = config.selectGame(RandomSource.create());
        if (game == null) {
            throw new GameOpenException(Component.translatable("text.plasmid.random.empty_composite_game_config"));
        }

        return GameOpenProcedure.withOverride(GameConfig.openProcedure(context.server(), game), game);
    }
}
