package xyz.nucleoid.plasmid.impl.game.composite;

import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

public final class RandomGame {
    public static GameOpenProcedure open(GameOpenContext<RandomGameConfig> context) {
        var config = context.config();

        if(config.isEmpty()) {
            throw new GameOpenException(Text.translatable("text.plasmid.random.empty_composite_game_config"));
        }
        
        RegistryEntry<GameConfig<?>> game = null;
        while(game == null) game = config.selectGame(Random.createLocal());

        return GameOpenProcedure.withOverride(GameConfig.openProcedure(context.server(), game), game);
    }
}



