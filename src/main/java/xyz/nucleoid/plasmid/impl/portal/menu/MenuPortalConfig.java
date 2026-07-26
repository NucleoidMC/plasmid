package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record MenuPortalConfig(
        Component name,
        List<Component> description,
        ItemStackTemplate icon,
        List<Entry> games,
        CustomValuesConfig custom
) implements GamePortalConfig {

    public static final MapCodec<MenuPortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(MenuPortalConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description", Collections.emptyList()).forGetter(MenuPortalConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon", new ItemStackTemplate(Items.GRASS_BLOCK)).forGetter(MenuPortalConfig::icon),
            Entry.CODEC.listOf().fieldOf("games").forGetter(config -> config.games),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(config -> config.custom)
    ).apply(i, MenuPortalConfig::new));

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        Component name;
        if (this.name != null && this.name != CommonComponents.EMPTY) {
            name = this.name;
        } else {
            name = Component.literal(id.toString());
        }

        return new MenuPortalBackend(name, this.description, this.icon.create(), this.games);
    }

    @Override
    public MapCodec<MenuPortalConfig> codec() {
        return CODEC;
    }

    public record Entry(Holder<GameConfig<?>> game,
                        Optional<Component> name,
                        Optional<List<Component>> description,
                        Optional<ItemStackTemplate> icon) {

        static final Codec<Entry> CODEC_OBJECT = RecordCodecBuilder.create(i -> i.group(
                GameConfig.ENTRY_CODEC.fieldOf("game").forGetter(entry -> entry.game),
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(Entry::name),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(Entry::description),
                ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(Entry::icon)
        ).apply(i, Entry::new));

        public static final Codec<Entry> CODEC = Codec.either(GameConfig.ENTRY_CODEC, CODEC_OBJECT)
                .xmap(either -> either.map((game) -> new Entry(game, Optional.empty(), Optional.empty(), Optional.empty()), Function.identity()), Either::right);
    }
}
