package xyz.nucleoid.plasmid.api.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;

public final class PlasmidMessageTypes {
    public static final RegistryKey<MessageType> TEAM_CHAT = register("team_chat");

    private static RegistryKey<MessageType> register(String key) {
        return RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Plasmid.id(key));
    }
}
