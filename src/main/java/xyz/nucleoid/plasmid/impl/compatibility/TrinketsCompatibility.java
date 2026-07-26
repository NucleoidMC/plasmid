package xyz.nucleoid.plasmid.impl.compatibility;

import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.util.InventoryUtil;

import java.util.List;

public class TrinketsCompatibility {
    private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded("trinkets");


    public static void onInitialize() {
        if (!ENABLED) {
            return;
        }
        Plasmid.LOGGER.info("Trinkets compat loaded");

        InventoryUtil.addCustomHandler(new InventoryUtil.CustomInventoryHandler() {
            @Override
            public void clear(ServerPlayer player) {
                var attachment = TrinketsApi.getAttachment(player);

                for (var x : attachment.getInventory().values()) {
                    for (var y : x.values()) {
                        y.clearContent();
                    }
                }
            }
        });
    }
}
