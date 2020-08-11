package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public final class ConfiguredGame<C> {
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

    public CompletableFuture<Void> open(MinecraftServer server) {
        return this.type.open(server, this.config);
    }

    public GameType<C> getType() {
        return this.type;
    }

    public C getConfig() {
        return this.config;
    }

    @SuppressWarnings("unchecked")
    private static <T> T coerceConfigUnchecked(Object config) {
        return (T) config;
    }
}
