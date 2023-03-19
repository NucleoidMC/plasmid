package xyz.nucleoid.plasmid.game.portal.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;

public record GameMenuEntryConfig(
        RegistryEntry<GameConfig<?>> game,
        Optional<Text> name,
        Optional<List<Text>> description,
        Optional<ItemStack> icon
) implements MenuEntryConfig {
    public static final Codec<GameMenuEntryConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            GameConfig.CODEC.fieldOf("game").forGetter(GameMenuEntryConfig::game),
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(GameMenuEntryConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(GameMenuEntryConfig::description),
            MoreCodecs.ITEM_STACK.optionalFieldOf("icon").forGetter(GameMenuEntryConfig::icon)
    ).apply(i, GameMenuEntryConfig::new));

    @Override
    public MenuEntry createEntry() {
        var game = new ConcurrentGamePortalBackend(this.game);
        return new GameMenuEntry(
                game,
                this.name.orElse(GameConfig.name(this.game)),
                this.description.orElse(this.game.value().description()),
                this.icon.orElse(this.game.value().icon())
        );
    }

    @Override
    public Codec<? extends MenuEntryConfig> codec() {
        return CODEC;
    }
}