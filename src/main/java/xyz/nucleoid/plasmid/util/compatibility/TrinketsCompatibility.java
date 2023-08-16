package xyz.nucleoid.plasmid.util.compatibility;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.data.EntitySlotLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.util.InventoryUtil;

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
            public void clear(ServerPlayerEntity player) {
                var component = TrinketsApi.getTrinketComponent(player);
                if (component.isEmpty()) {
                    return;
                }

                for (var x : component.get().getInventory().values()) {
                    for (var y : x.values()) {
                        y.clear();
                    }
                }
                component.get().getInventory().clear();
                component.get().getTrackingUpdates().forEach(TrinketInventory::clear);
                component.get().getGroups().clear();
                component.get().update();
                EntitySlotLoader.SERVER.sync(List.of(player));
            }
        });
    }

}
