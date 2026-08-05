package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.menu.layout.FixedLayout;
import xyz.nucleoid.plasmid.api.menu.layout.FlowLayout;
import xyz.nucleoid.plasmid.api.menu.layout.RowsLayout;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;

/**
 * A kind of {@link GameMenuLayout}, registered so datapacks can name it and mods can add their own.
 *
 * <p>The ordinary dispatch-on-{@code type} registry, holding one {@code MapCodec} per kind the way
 * {@code TreeDecoratorType} does. Codecs are built once at registration rather than per decode.
 *
 * <p>A layout is generic in what it holds, but only ever decoded over the one element type a datapack can
 * write, so the codec fixes that and {@link GameMenuLayout#map} carries a decoded layout to whatever the
 * menu is built from.
 */
public final class GameMenuLayoutType {
    /**
     * A flat list. Also what a bare JSON list decodes to.
     */
    public static final GameMenuLayoutType FLOW = register("flow", FlowLayout.CODEC);

    /**
     * A list of rows.
     */
    public static final GameMenuLayoutType ROWS = register("rows", RowsLayout.CODEC);

    /**
     * Explicit coordinates. Also what a bare JSON {@code "x:y"} map decodes to.
     */
    public static final GameMenuLayoutType FIXED = register("fixed", FixedLayout.CODEC);

    private final MapCodec<? extends GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec;

    private GameMenuLayoutType(MapCodec<? extends GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec() {
        return this.codec;
    }

    public static GameMenuLayoutType register(Identifier key, MapCodec<? extends GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec) {
        return Registry.register(PlasmidRegistries.GAME_MENU_LAYOUT_TYPE, key, new GameMenuLayoutType(codec));
    }

    private static GameMenuLayoutType register(String key, MapCodec<? extends GameMenuLayout<HolderSet<GameMenuEntryConfig>>> codec) {
        return register(Plasmid.id(key), codec);
    }
}
