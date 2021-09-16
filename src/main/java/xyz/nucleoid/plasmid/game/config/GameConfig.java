package xyz.nucleoid.plasmid.game.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameType;

import java.util.function.Function;
import java.util.stream.Stream;

public final class GameConfig<C> {
    public static final Codec<GameConfig<?>> CODEC = new ConfigCodec(null).codec();

    @Nullable
    private final Identifier source;

    private final GameType<C> type;
    @Nullable
    private final String name;
    @Nullable
    private final String translation;

    private final CustomValuesConfig custom;

    private final C config;

    private GameConfig(
            @Nullable Identifier source,
            GameType<C> type,
            @Nullable String name,
            @Nullable String translation,
            CustomValuesConfig custom,
            C config
    ) {
        this.source = source;
        this.type = type;
        this.name = name;
        this.translation = translation;
        this.custom = custom;
        this.config = config;
    }

    public static Codec<GameConfig<?>> codecFrom(Identifier source) {
        return new ConfigCodec(source).codec();
    }

    public GameOpenProcedure openProcedure(MinecraftServer server) {
        var context = new GameOpenContext<C>(server, this);
        return this.type.open(context);
    }

    /**
     * @return the source location that this config was loaded from, if loaded from a file.
     */
    @Nullable
    public Identifier source() {
        return this.source;
    }

    public GameType<C> type() {
        return this.type;
    }

    /**
     * @return the name for this game config, defaulted to the game type name if none is specified
     */
    public Text name() {
        if (this.name != null) {
            return new LiteralText(this.name);
        } else if (this.translation != null) {
            return new TranslatableText(this.translation);
        } else {
            return this.type.name();
        }
    }

    public CustomValuesConfig custom() {
        return this.custom;
    }

    public C config() {
        return this.config;
    }

    static final class ConfigCodec extends MapCodec<GameConfig<?>> {
        private final Identifier source;

        ConfigCodec(Identifier source) {
            this.source = source;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(
                    ops.createString("type"),
                    ops.createString("name"),
                    ops.createString("translation"),
                    ops.createString("config")
            );
        }

        @Override
        public <T> DataResult<GameConfig<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
            var typeResult = GameType.REGISTRY.decode(ops, input.get("type")).map(Pair::getFirst);

            return typeResult.flatMap(type -> {
                var name = this.decodeStringOrNull(ops, input.get("name"));
                var translation = this.decodeStringOrNull(ops, input.get("translation"));
                var custom = this.decodeCustomValues(ops, input.get("custom"));

                var configCodec = type.configCodec();
                return this.decodeConfig(ops, input, configCodec).map(config -> {
                    return this.createConfigUnchecked(type, name, translation, custom, config);
                });
            });
        }

        private <T> String decodeStringOrNull(DynamicOps<T> ops, T input) {
            return Codec.STRING.decode(ops, input)
                    .result().map(Pair::getFirst)
                    .orElse(null);
        }

        private <T> CustomValuesConfig decodeCustomValues(DynamicOps<T> ops, T input) {
            return CustomValuesConfig.CODEC.decode(ops, input)
                    .result().map(Pair::getFirst)
                    .orElse(CustomValuesConfig.empty());
        }

        private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
            if (configCodec instanceof MapCodecCodec<?> mapCodec) {
                return mapCodec.codec().decode(ops, input).map(Function.identity());
            } else {
                return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
            }
        }

        @SuppressWarnings("unchecked")
        private <C> GameConfig<C> createConfigUnchecked(GameType<C> type, String name, String translation, CustomValuesConfig custom, Object config) {
            return new GameConfig<>(this.source, type, name, translation, custom, (C) config);
        }

        @Override
        public <T> RecordBuilder<T> encode(GameConfig<?> game, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return this.encodeUnchecked(game, ops, prefix);
        }

        private <T, C> RecordBuilder<T> encodeUnchecked(GameConfig<C> game, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            var codec = game.type.configCodec();
            if (codec instanceof MapCodecCodec<C> mapCodec) {
                prefix = mapCodec.codec().encode(game.config, ops, prefix);
            } else {
                prefix.add("config", codec.encodeStart(ops, game.config));
            }

            prefix.add("type", GameType.REGISTRY.encodeStart(ops, game.type));

            if (game.name != null) {
                prefix.add("name", Codec.STRING.encodeStart(ops, game.name));
            }

            if (game.translation != null) {
                prefix.add("translation", Codec.STRING.encodeStart(ops, game.translation));
            }

            if (!game.custom.isEmpty()) {
                prefix.add("custom", CustomValuesConfig.CODEC.encodeStart(ops, game.custom));
            }

            return prefix;
        }
    }
}
