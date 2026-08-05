package xyz.nucleoid.plasmid.impl.menu.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.menu.GameMenuConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;
import xyz.nucleoid.plasmid.impl.menu.GameMenuEntries;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@code plasmid:hybrid}: one entry, two destinations. Left-click takes {@code main}, right-click
 * {@code alt}. The usual "join a game / browse the modes" pairing.
 */
public record HybridEntryConfig(
        Holder<GameMenuEntryConfig> main,
        Holder<GameMenuEntryConfig> alt,
        Optional<Component> altMessage,
        Optional<Component> name,
        Optional<List<Component>> description,
        Optional<ItemStackTemplate> icon
) implements GameMenuEntryConfig {
    public static final MapCodec<HybridEntryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuEntryConfig.ENTRY_CODEC.fieldOf("main").forGetter(HybridEntryConfig::main),
            GameMenuEntryConfig.ENTRY_CODEC.fieldOf("alt").forGetter(HybridEntryConfig::alt),
            PlasmidCodecs.TEXT.optionalFieldOf("alt_message").forGetter(HybridEntryConfig::altMessage),
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(HybridEntryConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(HybridEntryConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(HybridEntryConfig::icon)
    ).apply(i, HybridEntryConfig::new));

    @Override
    public GameMenuEntry createEntry() {
        var main = GameMenuEntries.of(this.main.value());
        var alt = GameMenuEntries.of(this.alt.value());
        var altMessage = this.altMessage.orElseGet(() -> Component.translatable("text.plasmid.ui.game_menu.more"));

        return new HybridEntry(
                main,
                alt,
                new GameMenuEntry.Action(altMessage, altMessage),
                this.name.orElseGet(main::name),
                this.description.orElseGet(main::description),
                this.icon.map(ItemStackTemplate::create).orElseGet(() -> main.icon().copy())
        );
    }

    /**
     * Both sides, since either button can be the one that opens a menu.
     */
    @Override
    public void provideMenus(Consumer<Holder<GameMenuConfig>> consumer) {
        this.main.value().provideMenus(consumer);
        this.alt.value().provideMenus(consumer);
    }

    @Override
    public MapCodec<HybridEntryConfig> codec() {
        return CODEC;
    }
}
