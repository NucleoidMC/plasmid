package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;
import xyz.nucleoid.plasmid.impl.menu.GameMenuEntries;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * A datapack-defined menu, at {@code data/<namespace>/plasmid/game_menu/*.json}.
 * <p>
 * A menu is its entries and nothing else: what a slot leads to is the entry's business, so the same menu
 * definition serves whether an entry joins a game, opens another menu, or is supplied by a mod.
 *
 * <pre>{@code
 * { "title": {"translate": "gameType.uhc.uhc"}, "entries": ["#uhc:modes"] }
 * }</pre>
 *
 * @param entries a layout over entry references, tags, or inline definitions
 * @param size {@code "fit"}, {@code "inherit"}, or a region such as {@code "9x4"}
 * @param features overrides what would otherwise be derived from the contents
 * @param theme overrides the theme inherited from the menu this was opened from
 */
public record GameMenuConfig(
        Component title,
        GameMenuLayout<HolderSet<GameMenuEntryConfig>> entries,
        GameMenuSizeRule size,
        Optional<List<GameMenuFeature>> features,
        Optional<Holder<GameMenuTheme>> theme
) {
    public static final Codec<GameMenuConfig> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            PlasmidCodecs.TEXT.fieldOf("title").forGetter(GameMenuConfig::title),
            GameMenuLayout.CODEC.fieldOf("entries").forGetter(GameMenuConfig::entries),
            GameMenuSizeRule.CODEC.optionalFieldOf("size", GameMenuSizeRule.Auto.FIT).forGetter(GameMenuConfig::size),
            GameMenuFeature.CODEC.listOf().optionalFieldOf("features").forGetter(GameMenuConfig::features),
            GameMenuTheme.ENTRY_CODEC.optionalFieldOf("theme").forGetter(GameMenuConfig::theme)
    ).apply(i, GameMenuConfig::new));

    public static final Codec<Holder<GameMenuConfig>> ENTRY_CODEC = RegistryFileCodec.create(PlasmidRegistryKeys.GAME_MENU, DIRECT_CODEC);

    /**
     * Resolves the entries and builds the menu.
     * <p>
     * Callers are expected to hold on to the result: entries carry live state, so building twice would give
     * a game two separate views of the same thing.
     */
    public GameMenu build() {
        var builder = GameMenu.builder(this.title)
                .layout(this.entries.flatMap(set -> set.stream()
                        .map(holder -> GameMenuElement.of(GameMenuEntries.of(holder.value())))
                        .toList()))
                .sizeRule(this.size)
                .theme(this.theme.orElse(null));

        this.features.ifPresent(features -> builder.features(new LinkedHashSet<>(features).toArray(new GameMenuFeature[0])));

        return builder.build();
    }
}
