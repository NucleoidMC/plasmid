package xyz.nucleoid.plasmid.api.chat;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.impl.Plasmid;

public final class PlasmidMessageTypes {
    public static final ResourceKey<ChatType> TEAM_CHAT = createKey("team_chat");

    private static ResourceKey<ChatType> createKey(String key) {
        return ResourceKey.create(Registries.CHAT_TYPE, Plasmid.id(key));
    }
}
