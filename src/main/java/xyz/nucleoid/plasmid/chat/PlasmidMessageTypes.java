package xyz.nucleoid.plasmid.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;

public final class PlasmidMessageTypes {
    public static final RegistryKey<MessageType> TEAM_CHAT = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier(Plasmid.ID, "team_chat"));
}
