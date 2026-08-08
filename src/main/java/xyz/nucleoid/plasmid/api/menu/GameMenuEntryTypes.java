package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.menu.entry.GameEntryConfig;
import xyz.nucleoid.plasmid.impl.menu.entry.HybridEntryConfig;
import xyz.nucleoid.plasmid.impl.menu.entry.MenuEntryConfig;

public final class GameMenuEntryTypes {
    public static final MapCodec<? extends GameMenuEntryConfig> GAME = register("game", GameEntryConfig.CODEC);
    public static final MapCodec<? extends GameMenuEntryConfig> MENU = register("menu", MenuEntryConfig.CODEC);
    public static final MapCodec<? extends GameMenuEntryConfig> HYBRID = register("hybrid", HybridEntryConfig.CODEC);

    private GameMenuEntryTypes() {
    }

    public static MapCodec<? extends GameMenuEntryConfig> register(Identifier key, MapCodec<? extends GameMenuEntryConfig> codec) {
        return Registry.register(PlasmidRegistries.GAME_MENU_ENTRY_TYPE, key, codec);
    }

    private static MapCodec<? extends GameMenuEntryConfig> register(String key, MapCodec<? extends GameMenuEntryConfig> codec) {
        return register(Plasmid.id(key), codec);
    }
}
