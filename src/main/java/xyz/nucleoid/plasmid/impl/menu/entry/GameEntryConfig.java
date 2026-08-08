package xyz.nucleoid.plasmid.impl.menu.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;

/**
 * {@code plasmid:game}: joins a game, under the given {@link GameJoinMode mode}.
 */
public record GameEntryConfig(
        Holder<GameConfig<?>> game,
        GameJoinMode mode,
        Optional<Component> name,
        Optional<List<Component>> description,
        Optional<ItemStackTemplate> icon
) implements GameMenuEntryConfig {
    public static final MapCodec<GameEntryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameConfig.ENTRY_CODEC.fieldOf("game").forGetter(GameEntryConfig::game),
            GameJoinMode.CODEC.optionalFieldOf("mode", GameJoinMode.CONCURRENT).forGetter(GameEntryConfig::mode),
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(GameEntryConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(GameEntryConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(GameEntryConfig::icon)
    ).apply(i, GameEntryConfig::new));

    @Override
    public GameMenuEntry createEntry() {
        return new GameEntry(
                this.game,
                this.mode,
                this.name.orElseGet(() -> GameConfig.name(this.game)),
                this.description.orElseGet(() -> this.game.value().description()),
                this.icon.map(ItemStackTemplate::create).orElseGet(() -> this.game.value().icon())
        );
    }

    @Override
    public MapCodec<GameEntryConfig> codec() {
        return CODEC;
    }
}
