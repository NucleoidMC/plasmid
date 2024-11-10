package xyz.nucleoid.plasmid.test;

import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;

public class PlayerlessGame {
    public static GameOpenProcedure open(GameOpenContext context) {
        return context.openWithWorld(new RuntimeWorldConfig(), (activity, world) -> {});
    }
}
