package xyz.nucleoid.plasmid.impl.portal.config;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;
import xyz.nucleoid.plasmid.impl.portal.backend.menu.MenuPortalBackend;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record MenuPortalConfig(
        Text name,
        List<Text> description,
        ItemStack icon,
        List<Entry> games,
        CustomValuesConfig custom
) implements GamePortalConfig {

    public static final MapCodec<MenuPortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name", ScreenTexts.EMPTY).forGetter(MenuPortalConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description", Collections.emptyList()).forGetter(MenuPortalConfig::description),
            MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(MenuPortalConfig::icon),
            Entry.CODEC.listOf().fieldOf("games").forGetter(config -> config.games),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(config -> config.custom)
    ).apply(i, MenuPortalConfig::new));

    @Override
    public MapCodec<MenuPortalConfig> codec() {
        return CODEC;
    }

    public record Entry(RegistryEntry<GameConfig<?>> game,
                        Optional<Text> name,
                        Optional<List<Text>> description,
                        Optional<ItemStack> icon) {

        static final Codec<Entry> CODEC_OBJECT = RecordCodecBuilder.create(i -> i.group(
                GameConfig.CODEC.fieldOf("game").forGetter(entry -> entry.game),
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Entry::name),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Entry::description),
                MoreCodecs.ITEM_STACK.optionalFieldOf("icon").forGetter(Entry::icon)
        ).apply(i, Entry::new));

        public static final Codec<Entry> CODEC = Codec.either(GameConfig.CODEC, CODEC_OBJECT)
                .xmap(either -> either.map((game) -> new Entry(game, Optional.empty(), Optional.empty(), Optional.empty()), Function.identity()), Either::right);
    }
}
