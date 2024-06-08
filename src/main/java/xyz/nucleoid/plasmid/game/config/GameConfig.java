package xyz.nucleoid.plasmid.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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

public record GameConfig<C>(
        GameType<C> type,
        @Nullable Text name,
        @Nullable Text shortName,
        @Nullable List<Text> description,
        @Nullable ItemStack icon,
        CustomValuesConfig custom,
        C config
) {
    public static final Codec<GameConfig<?>> DIRECT_CODEC = GameType.REGISTRY.dispatch(GameConfig::type, GameConfig::createTypedCodec);
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

    private static <C> MapCodec<GameConfig<C>> createTypedCodec(GameType<C> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                type.configCodec().forGetter(GameConfig::config),
                Metadata.MAP_CODEC.forGetter(Metadata::from)
        ).apply(i, (config, metadata) -> new GameConfig<>(
                type,
                metadata.name.orElse(null),
                metadata.shortName.orElse(null),
                metadata.description.orElse(null),
                metadata.icon,
                metadata.custom,
                config
        )));
    }

    private record Metadata(
            Optional<Text> name,
            Optional<Text> shortName,
            Optional<List<Text>> description,
            ItemStack icon,
            CustomValuesConfig custom
    ) {
        static final MapCodec<Metadata> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Metadata::name),
                PlasmidCodecs.TEXT.optionalFieldOf("short_name").forGetter(Metadata::shortName),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Metadata::description),
                MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(Metadata::icon),
                CustomValuesConfig.CODEC.fieldOf("custom").orElseGet(CustomValuesConfig::empty).forGetter(Metadata::custom)
        ).apply(i, Metadata::new));

        public static Metadata from(GameConfig<?> game) {
            return new Metadata(
                    Optional.ofNullable(game.name),
                    Optional.ofNullable(game.shortName),
                    Optional.ofNullable(game.description),
                    game.icon,
                    game.custom
            );
        }
    }
}
