package xyz.nucleoid.plasmid.game;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.error.ErrorReporter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ConfiguredGame<C> {
    public static final Codec<ConfiguredGame<?>> CODEC = new ConfigCodec().codec();

    private final GameType<C> type;
    @Nullable
    private final String name;
    private final C config;

    private ConfiguredGame(GameType<C> type, @Nullable String name, C config) {
        this.type = type;
        this.name = name;
        this.config = config;
    }

    public GameOpenProcedure openProcedure(MinecraftServer server) {
        GameOpenContext<C> context = new GameOpenContext<>(server, this);
        return this.type.open(context);
    }

    public CompletableFuture<ManagedGameSpace> open(MinecraftServer server) {
        CompletableFuture<ManagedGameSpace> future = CompletableFuture.supplyAsync(() -> this.openProcedure(server), Util.getMainWorkerExecutor())
                .thenCompose(GameOpenProcedure::open);

        future.exceptionally(throwable -> {
            try (ErrorReporter reporter = ErrorReporter.open(this)) {
                reporter.report(throwable, "Opening game");
            }
            return null;
        });

        return future;
    }

    public GameType<C> getType() {
        return this.type;
    }

    // TODO: Remove in 0.5 - replaced with getOptionalName and getDisplayName below
    @Deprecated
    public String getName() {
        return this.name != null ? this.name : this.type.getIdentifier().toString();
    }

    /**
     * @return An {@link Optional} containing the name of the game config, if specified.
     */
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(this.name);
    }

    /**
     * @param id The game ID of the current {@link ConfiguredGame}
     * @return The name of the game as specified in the config, or the provided {@link Identifier} if it was not.
     */
    public String getDisplayName(Identifier id) {
        return this.getOptionalName().orElseGet(id::toString);
    }

    public C getConfig() {
        return this.config;
    }

    static final class ConfigCodec extends MapCodec<ConfiguredGame<?>> {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("type"), ops.createString("name"), ops.createString("config"));
        }

        @Override
        public <T> DataResult<ConfiguredGame<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
            DataResult<GameType<?>> typeResult = GameType.REGISTRY.decode(ops, input.get("type")).map(Pair::getFirst);

            return typeResult.flatMap(type -> {
                String name = Codec.STRING.decode(ops, input.get("name"))
                        .result().map(Pair::getFirst)
                        .orElse(null);

                Codec<?> configCodec = type.getConfigCodec();
                return this.decodeConfig(ops, input, configCodec).map(config -> {
                    return createConfigUnchecked(type, name, config);
                });
            });
        }

        private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
            if (configCodec instanceof MapCodec.MapCodecCodec<?>) {
                return ((MapCodecCodec<?>) configCodec).codec().decode(ops, input).map(Function.identity());
            } else {
                return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
            }
        }

        @SuppressWarnings("unchecked")
        private static <C> ConfiguredGame<C> createConfigUnchecked(GameType<C> type, String name, Object config) {
            return new ConfiguredGame<>(type, name, (C) config);
        }

        @Override
        public <T> RecordBuilder<T> encode(ConfiguredGame<?> game, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return this.encodeUnchecked(game, ops, prefix);
        }

        private <T, C> RecordBuilder<T> encodeUnchecked(ConfiguredGame<C> game, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            Codec<C> codec = game.type.getConfigCodec();
            if (codec instanceof MapCodecCodec<?>) {
                prefix = ((MapCodecCodec<C>) codec).codec().encode(game.config, ops, prefix);
            } else {
                prefix.add("config", codec.encodeStart(ops, game.config));
            }

            prefix.add("type", GameType.REGISTRY.encodeStart(ops, game.type));
            prefix.add("name", Codec.STRING.encodeStart(ops, game.name));

            return prefix;
        }
    }
}
