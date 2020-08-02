package net.gegy1000.plasmid.game;

import net.gegy1000.plasmid.game.config.GameConfig;

public final class GameAndConfig<C extends GameConfig> {
    private final Game game;
    private final ConfiguredGame<C> configured;

    public GameAndConfig(Game game, ConfiguredGame<C> configured) {
        this.game = game;
        this.configured = configured;
    }

    public Game getGame() {
        return this.game;
    }

    public ConfiguredGame<C> getConfigured() {
        return this.configured;
    }
}
