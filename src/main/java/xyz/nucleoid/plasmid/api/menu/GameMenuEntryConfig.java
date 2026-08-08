package xyz.nucleoid.plasmid.api.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.menu.entry.GameEntryConfig;
import xyz.nucleoid.plasmid.impl.menu.entry.GameJoinMode;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Entries live in the {@code plasmid:game_menu_entry} dynamic registry, at
 * {@code data/<namespace>/plasmid/game_menu_entry/*.json}, so they can be tagged. A game declares once how
 * it wishes to appear, then adds itself to a menu by writing a tag file in its own mod:
 *
 * <pre>{@code
 * // data/my_game/plasmid/game_menu_entry/my_game.json
 * { "type": "plasmid:hybrid", "main": "my_game:solo", "alt": { "type": "plasmid:menu", "menu": "my_game:modes" } }
 *
 * // data/my_game/tags/plasmid/game_menu_entry/combat.json
 * { "values": [ "my_game:my_game" ] }
 * }</pre>
 *
 * <p>A menu names the tag and picks up every game that joins it, without either side being edited.
 *
 * @see GameMenuEntryTypes
 */
public interface GameMenuEntryConfig {
    Codec<GameMenuEntryConfig> TYPED_CODEC = PlasmidRegistries.GAME_MENU_ENTRY_TYPE.byNameCodec().dispatchStable(GameMenuEntryConfig::codec, Function.identity());

    Codec<GameMenuEntryConfig> DIRECT_CODEC = Codec.either(GameConfig.ENTRY_CODEC, TYPED_CODEC).xmap(
            either -> either.map(
                    game -> new GameEntryConfig(game, GameJoinMode.CONCURRENT, Optional.empty(), Optional.empty(), Optional.empty()),
                    Function.identity()),
            Either::right
    );
    Codec<Holder<GameMenuEntryConfig>> ENTRY_CODEC = RegistryFileCodec.create(PlasmidRegistryKeys.GAME_MENU_ENTRY, DIRECT_CODEC);
    Codec<HolderSet<GameMenuEntryConfig>> ENTRY_LIST_CODEC = RegistryCodecs.homogeneousList(PlasmidRegistryKeys.GAME_MENU_ENTRY, DIRECT_CODEC);

    GameMenuEntry createEntry();

    /**
     * The menus this entry leads to, named or written inline.
     *
     * <p>An inline menu appears in no registry, so nothing walking the tree would otherwise find it. Without
     * this it would quietly lose the load-time checks a named menu gets.
     */
    default void provideMenus(Consumer<Holder<GameMenuConfig>> consumer) {
    }

    MapCodec<? extends GameMenuEntryConfig> codec();
}
