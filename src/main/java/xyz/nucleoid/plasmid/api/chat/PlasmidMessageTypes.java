package xyz.nucleoid.plasmid.api.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

public final class PlasmidMessageTypes {
    public static final RegistryKey<MessageType> TEAM_CHAT = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of(Plasmid.ID, "team_chat"));
}
