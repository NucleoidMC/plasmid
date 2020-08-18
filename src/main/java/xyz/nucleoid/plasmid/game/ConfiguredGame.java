package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public final class ConfiguredGame<C> {
    public static final Codec<ConfiguredGame<?>> CODEC = GameType.REGISTRY.dispatchStable(c -> c.type, ConfiguredGame::codecFor);

    private final GameType<C> type;
    private final C config;

    private ConfiguredGame(GameType<C> type, C config) {
        this.type = type;
        this.config = config;
    }

    public CompletableFuture<GameWorld> open(MinecraftServer server) {
        GameOpenContext<C> context = new GameOpenContext<>(server, this);
        return this.type.open(context);
    }

    public GameType<C> getType() {
        return this.type;
    }

    public C getConfig() {
        return this.config;
    }

    private static <C> Codec<? extends ConfiguredGame<C>> codecFor(GameType<C> type) {
        Codec<C> configCodec = type.getConfigCodec();
        if (configCodec instanceof MapCodec.MapCodecCodec) {
            MapCodec<C> codec = ((MapCodec.MapCodecCodec<C>) configCodec).codec();
            return xmapMapCodec(type, codec).codec();
        } else {
            return xmapCodec(type, configCodec);
        }
    }

    private static <C> MapCodec<? extends ConfiguredGame<C>> xmapMapCodec(GameType<C> type, MapCodec<C> codec) {
        return codec.xmap(
                config -> new ConfiguredGame<>(type, config),
                configured -> configured.config
        );
    }

    private static <C> Codec<? extends ConfiguredGame<C>> xmapCodec(GameType<C> type, Codec<C> codec) {
        return codec.xmap(
                config -> new ConfiguredGame<>(type, config),
                configured -> configured.config
        );
    }
}
