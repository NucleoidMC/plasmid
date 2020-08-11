package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.concurrent.CompletableFuture;

public final class ConfiguredGame<C extends GameConfig> {
    public static final Codec<ConfiguredGame<?>> CODEC = GameType.REGISTRY.dispatchStable(
            o -> o.type,
            type -> type.getConfigCodec().xmap(
                    config -> new ConfiguredGame<>(type, coerceConfigUnchecked(config)),
                    tac -> coerceConfigUnchecked(tac.config)
            )
    );

    private final GameType<C> type;
    private final C config;

    private ConfiguredGame(GameType<C> type, C config) {
        this.type = type;
        this.config = config;
    }

    public CompletableFuture<Void> open(GameWorldState state) {
        return this.type.open(state, this.config);
    }

    public GameType<C> getType() {
        return this.type;
    }

    public C getConfig() {
        return this.config;
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameConfig> T coerceConfigUnchecked(GameConfig config) {
        return (T) config;
    }
}
