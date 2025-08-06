package xyz.nucleoid.plasmid.api.portal;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.game.LegacyOnDemandPortalConfig;
import xyz.nucleoid.plasmid.impl.portal.game.NewGamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.game.SingleGamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.AdvancedMenuPortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuPortalConfig;

public class GamePortalConfigs {
    public static MapCodec<? extends GamePortalConfig> SINGLE_GAME = register("single_game", SingleGamePortalConfig.CODEC);
    public static MapCodec<? extends GamePortalConfig> NEW_GAME = register("new_game", NewGamePortalConfig.CODEC);
    public static MapCodec<? extends GamePortalConfig> CONCURRENT_GAME = register("concurrent_game", ConcurrentGamePortalConfig.CODEC);
    public static MapCodec<? extends GamePortalConfig> ON_DEMAND = register("on_demand", LegacyOnDemandPortalConfig.CODEC);

    public static MapCodec<? extends GamePortalConfig> MENU = register("menu", MenuPortalConfig.CODEC);
    public static MapCodec<? extends GamePortalConfig> ADVANCED_MENU = register("advanced_menu", AdvancedMenuPortalConfig.CODEC);

    public static MapCodec<? extends GamePortalConfig> register(Identifier key, MapCodec<? extends GamePortalConfig> codec) {
        return Registry.register(PlasmidRegistries.GAME_PORTAL_CONFIG, key, codec);
    }

    private static MapCodec<? extends GamePortalConfig> register(String key, MapCodec<? extends GamePortalConfig> codec) {
        return register(Plasmid.id(key), codec);
    }
}
