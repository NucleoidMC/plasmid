package xyz.nucleoid.plasmid.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.entity.PlasmidPlayer;

public class Networking {

    public static final Identifier IS_USING_PLASMID_API = new Identifier("plasmid", "plasmid_client");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(IS_USING_PLASMID_API, (server, player, networkHandler, buf, sender) -> ((PlasmidPlayer) player).setUsingPlasmidApi(true));
    }
}
