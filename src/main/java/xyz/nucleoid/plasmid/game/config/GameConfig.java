package xyz.nucleoid.plasmid.game.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record GameConfig<C>(
        @Nullable Identifier source,
        GameType<C> type,
        @Nullable Text name,
        @Nullable Text shortName,
        @Nullable List<Text> description,
        @Nullable ItemStack icon,
        CustomValuesConfig custom,
        C config
) {
    public static final Codec<GameConfig<?>> CODEC = codecFrom(null);

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
    @Override
    @Nullable
    public Identifier source() {
        return this.source;
    }

    /**
     * @return the source location that this config was loaded from, if loaded from a file.
     */
    public static String sourceName(GameConfig<?> config) {
        return Optional.ofNullable(config.source).map(Identifier::toString).orElse("[unknown source]");
    }

    /**
     * @return the name for this game config, defaulted to the game type name if none is specified
     */
    public static Text name(final GameConfig<?> config) {
        var name = config.name;
        if (name != null) {
            return name;
        }

        var translationKey = config.translationKey();
        if (translationKey != null && hasTranslationFor(translationKey)) {
            return Text.translatable(translationKey);
        }

        return config.type.name();
    }

    /**
     * @return shortened version of the name, defaulted to standard name
     */
    public static Text shortName(final GameConfig<?> config) {
        if (config.shortName != null) {
            return config.shortName;
        }
        return name(config);
    }

    /**
     * @return provided description of game, defaults to empty list
     */
    @Override
    public List<Text> description() {
        if (this.description != null) {
            return this.description;
        }
        return Collections.emptyList();
    }

    /**
     * @return game configs icon, defaults to grass block
     */
    @Override
    public ItemStack icon() {
        if (this.icon != null) {
            return this.icon;
        }

        return Items.GRASS_BLOCK.getDefaultStack();
    }

    private static boolean hasTranslationFor(String translationKey) {
        var language = ServerLanguage.getLanguage(ServerLanguageDefinition.DEFAULT);
        return language.serverTranslations().contains(translationKey);
    }

    @Nullable
    public String translationKey() {
        if (this.source != null) {
            return Util.createTranslationKey("game", this.source);
        } else {
            return null;
        }
    }

    static final class ConfigCodec extends MapCodec<GameConfig<?>> {
        private final Identifier source;

        ConfigCodec(@Nullable Identifier source) {
            this.source = source;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(Stream.of(ops.createString("type"), ops.createString("config")), Metadata.MAP_CODEC.keys(ops));
        }

        @Override
        public <T> DataResult<GameConfig<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
            var typeResult = GameType.REGISTRY.decode(ops, input.get("type")).map(Pair::getFirst);

            return typeResult.flatMap(type -> {
                return Metadata.MAP_CODEC.decode(ops, input).flatMap(metadata -> {
                    return this.decodeConfig(ops, input, type.configCodec()).map(config -> {
                        return this.createConfigUnchecked(type, metadata, config);
                    });
                });
            });
        }

        private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
            if (configCodec instanceof MapCodecCodec<?> mapCodec) {
                return mapCodec.codec().decode(ops, input).map(Function.identity());
            } else {
                return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
            }
        }

        @SuppressWarnings("unchecked")
        private <C> GameConfig<C> createConfigUnchecked(GameType<C> type, Metadata metadata, Object config) {
            return new GameConfig<>(
                    this.source, type,
                    metadata.name.orElse(null), metadata.shortName.orElse(null), metadata.description.orElse(null),
                    metadata.icon, metadata.custom,
                    (C) config
            );
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

            var metadata = new Metadata(
                    Optional.ofNullable(game.name), Optional.ofNullable(game.shortName), Optional.ofNullable(game.description),
                    game.icon, game.custom
            );
            prefix = Metadata.MAP_CODEC.encode(metadata, ops, prefix);

            return prefix;
        }
    }

    static final record Metadata(
            Optional<Text> name, Optional<Text> shortName, Optional<List<Text>> description,
            ItemStack icon, CustomValuesConfig custom
    ) {
        static final MapCodec<Metadata> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                    PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Metadata::name),
                    PlasmidCodecs.TEXT.optionalFieldOf("short_name").forGetter(Metadata::shortName),
                    MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Metadata::description),
                    MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(Metadata::icon),
                    CustomValuesConfig.CODEC.fieldOf("custom").orElseGet(CustomValuesConfig::empty).forGetter(Metadata::custom)
            ).apply(instance, Metadata::new);
        });
    }
}
