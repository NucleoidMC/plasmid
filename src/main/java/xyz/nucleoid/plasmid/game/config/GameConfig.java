package xyz.nucleoid.plasmid.game.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
        GameType<C> type,
        @Nullable Text name,
        @Nullable Text shortName,
        @Nullable List<Text> description,
        @Nullable ItemStack icon,
        CustomValuesConfig custom,
        C config
) {
    public static final Codec<GameConfig<?>> DIRECT_CODEC = new ConfigCodec().codec();
    public static final Codec<RegistryEntry<GameConfig<?>>> CODEC = RegistryElementCodec.of(GameConfigs.REGISTRY_KEY, DIRECT_CODEC);

    public GameOpenProcedure openProcedure(MinecraftServer server) {
        var context = new GameOpenContext<C>(server, this);
        return this.type.open(context);
    }

    /**
     * @return the source location that this config was loaded from, if loaded from a file.
     */
    public static String sourceName(RegistryEntry<GameConfig<?>> config) {
        return config.getKey().map(e -> e.getValue().toString()).orElse("[unknown source]");
    }

    /**
     * @return the name for this game config, defaulted to the game type name if none is specified
     */
    public static Text name(final RegistryEntry<GameConfig<?>> config) {
        var name = config.value().name;
        if (name != null) {
            return name;
        }

        var translationKey = config.getKey().map(key -> Util.createTranslationKey("game", key.getValue()))
                .filter(GameConfig::hasTranslationFor);
        if (translationKey.isPresent()) {
            return Text.translatable(translationKey.get());
        }

        return config.value().type.name();
    }

    /**
     * @return shortened version of the name, defaulted to standard name
     */
    public static Text shortName(final RegistryEntry<GameConfig<?>> config) {
        if (config.value().shortName != null) {
            return config.value().shortName;
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

    static final class ConfigCodec extends MapCodec<GameConfig<?>> {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(Stream.of(ops.createString("type")), Metadata.MAP_CODEC.keys(ops));
        }

        @Override
        public <T> DataResult<GameConfig<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
            if (ops.compressMaps()) {
                return DataResult.error(() -> "Does not support compressed ops");
            }

            var typeResult = GameType.REGISTRY.decode(ops, input.get("type")).map(Pair::getFirst);

            return typeResult.flatMap(type ->
                    Metadata.MAP_CODEC.decode(ops, input).flatMap(metadata ->
                            this.decodeConfig(ops, input, type.configCodec()).map(config ->
                                    this.createConfigUnchecked(type, metadata, config)
                            )
                    )
            );
        }

        private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, MapCodec<?> configCodec) {
            return configCodec.decode(ops, input).map(Function.identity());
        }

        @SuppressWarnings("unchecked")
        private <C> GameConfig<C> createConfigUnchecked(GameType<C> type, Metadata metadata, Object config) {
            return new GameConfig<>(
                    type,
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
            prefix = game.type.configCodec().encode(game.config, ops, prefix);
            prefix.add("type", GameType.REGISTRY.encodeStart(ops, game.type));

            var metadata = new Metadata(
                    Optional.ofNullable(game.name), Optional.ofNullable(game.shortName), Optional.ofNullable(game.description),
                    game.icon, game.custom
            );
            prefix = Metadata.MAP_CODEC.encode(metadata, ops, prefix);

            return prefix;
        }
    }

    record Metadata(
            Optional<Text> name, Optional<Text> shortName, Optional<List<Text>> description,
            ItemStack icon, CustomValuesConfig custom
    ) {
        static final MapCodec<Metadata> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Metadata::name),
                PlasmidCodecs.TEXT.optionalFieldOf("short_name").forGetter(Metadata::shortName),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Metadata::description),
                MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(Metadata::icon),
                CustomValuesConfig.CODEC.fieldOf("custom").orElseGet(CustomValuesConfig::empty).forGetter(Metadata::custom)
        ).apply(i, Metadata::new));
    }
}
