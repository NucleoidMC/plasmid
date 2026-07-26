package xyz.nucleoid.plasmid.api.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;
import xyz.nucleoid.plasmid.impl.PlasmidConfig;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record GameConfig<C>(
        GameType<C> type,
        @Nullable Component name,
        @Nullable Component shortName,
        @Nullable List<Component> description,
        @Nullable ItemStackTemplate iconTemplate,
        CustomValuesConfig custom,
        C config
) {
    public static final Codec<GameConfig<?>> DIRECT_CODEC = PlasmidRegistries.GAME_TYPE.byNameCodec().dispatch(GameConfig::type, GameConfig::createTypedCodec);

    @Deprecated(forRemoval = true)
    public static final Codec<GameConfig<?>> REGISTRY_CODEC = Codec.lazyInitialized(() -> {
        if (!PlasmidConfig.get().ignoreInvalidGames()) {
            return DIRECT_CODEC;
        }

        return Codec.withAlternative(DIRECT_CODEC, MapCodec.unitCodec(() -> new GameConfig<>(
                GameTypes.INVALID,
                null,
                null,
                null,
                null,
                null,
                ""
        )));
    });
    public static final Codec<Holder<GameConfig<?>>> ENTRY_CODEC = RegistryFileCodec.create(PlasmidRegistryKeys.GAME_CONFIG, DIRECT_CODEC);
    public static final Codec<HolderSet<GameConfig<?>>> ENTRY_LIST_CODEC = RegistryCodecs.homogeneousList(PlasmidRegistryKeys.GAME_CONFIG);

    public static GameOpenProcedure openProcedure(MinecraftServer server, Holder<GameConfig<?>> config) {
        //noinspection unchecked,rawtypes
        var context = new GameOpenContext(server, config);
        //noinspection unchecked
        return config.value().type().open(context);
    }

    /**
     * @return the source location that this config was loaded from, if loaded from a file.
     */
    public static String sourceName(Holder<GameConfig<?>> config) {
        return config.unwrapKey().map(e -> e.identifier().toString()).orElse("[unknown source]");
    }

    /**
     * @return the name for this game config, defaulted to the game type name if none is specified
     */
    public static Component name(final Holder<GameConfig<?>> config) {
        var name = config.value().name;
        if (name != null) {
            return name;
        }

        var translationKey = config.unwrapKey().map(key -> Util.makeDescriptionId("game", key.identifier()))
                .filter(GameConfig::hasTranslationFor);
        if (translationKey.isPresent()) {
            return Component.translatable(translationKey.get());
        }

        return config.value().type.name();
    }

    /**
     * @return shortened version of the name, defaulted to standard name
     */
    public static Component shortName(final Holder<GameConfig<?>> config) {
        if (config.value().shortName != null) {
            return config.value().shortName;
        }
        return name(config);
    }

    /**
     * @return provided description of game, defaults to empty list
     */
    @Override
    public List<Component> description() {
        if (this.description != null) {
            return this.description;
        }
        return Collections.emptyList();
    }

    /**
     * @return game configs icon, defaults to grass block
     */
    public ItemStack icon() {
        if (this.iconTemplate != null) {
            return this.iconTemplate.create();
        }

        return Items.GRASS_BLOCK.getDefaultInstance();
    }

    private static boolean hasTranslationFor(String translationKey) {
        var language = ServerLanguage.getLanguage(ServerLanguageDefinition.DEFAULT);
        return language.serverTranslations().contains(translationKey);
    }

    private static <C> MapCodec<GameConfig<C>> createTypedCodec(GameType<C> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                type.configCodec().forGetter(GameConfig::config),
                Metadata.MAP_CODEC.forGetter(Metadata::from)
        ).apply(i, (config, metadata) -> new GameConfig<>(
                type,
                metadata.name.orElse(null),
                metadata.shortName.orElse(null),
                metadata.description.orElse(null),
                metadata.icon != null ? metadata.icon : new ItemStackTemplate(Items.GRASS_BLOCK),
                metadata.custom,
                config
        )));
    }

    private record Metadata(
            Optional<Component> name,
            Optional<Component> shortName,
            Optional<List<Component>> description,
            ItemStackTemplate icon,
            CustomValuesConfig custom
    ) {
        static final MapCodec<Metadata> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Metadata::name),
                PlasmidCodecs.TEXT.optionalFieldOf("short_name").forGetter(Metadata::shortName),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Metadata::description),
                ItemStackTemplate.CODEC.optionalFieldOf("icon", new ItemStackTemplate(Items.GRASS_BLOCK)).forGetter(Metadata::icon),
                CustomValuesConfig.CODEC.fieldOf("custom").orElseGet(CustomValuesConfig::empty).forGetter(Metadata::custom)
        ).apply(i, Metadata::new));

        public static Metadata from(GameConfig<?> game) {
            return new Metadata(
                    Optional.ofNullable(game.name),
                    Optional.ofNullable(game.shortName),
                    Optional.ofNullable(game.description),
                    game.iconTemplate != null ? game.iconTemplate : new ItemStackTemplate(Items.GRASS_BLOCK),
                    game.custom
            );
        }
    }
}
