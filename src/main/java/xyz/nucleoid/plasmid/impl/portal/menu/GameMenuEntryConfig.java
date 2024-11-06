package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;

public record GameMenuEntryConfig(
        RegistryEntry<GameConfig<?>> game,
        Optional<Text> name,
        Optional<List<Text>> description,
        Optional<ItemStack> icon
) implements MenuEntryConfig {
    public static final MapCodec<GameMenuEntryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
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
    public MapCodec<GameMenuEntryConfig> codec() {
        return CODEC;
    }
}
