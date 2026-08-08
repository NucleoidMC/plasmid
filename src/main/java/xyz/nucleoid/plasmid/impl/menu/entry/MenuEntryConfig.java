package xyz.nucleoid.plasmid.impl.menu.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.menu.GameMenuConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@code plasmid:menu}: opens another menu, which is how the tree is built.
 */
public record MenuEntryConfig(
        Holder<GameMenuConfig> menu,
        Optional<Component> name,
        Optional<List<Component>> description,
        Optional<ItemStackTemplate> icon
) implements GameMenuEntryConfig {
    public static final MapCodec<MenuEntryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuConfig.ENTRY_CODEC.fieldOf("menu").forGetter(MenuEntryConfig::menu),
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(MenuEntryConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(MenuEntryConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(MenuEntryConfig::icon)
    ).apply(i, MenuEntryConfig::new));

    @Override
    public GameMenuEntry createEntry() {
        return new MenuEntry(
                this.menu,
                this.name.orElseGet(() -> this.menu.value().title()),
                this.description.orElse(List.of()),
                this.icon.map(ItemStackTemplate::create).orElseGet(() -> Items.CHEST.getDefaultInstance())
        );
    }

    @Override
    public void provideMenus(Consumer<Holder<GameMenuConfig>> consumer) {
        consumer.accept(this.menu);
    }

    @Override
    public MapCodec<MenuEntryConfig> codec() {
        return CODEC;
    }
}
