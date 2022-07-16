package xyz.nucleoid.plasmid.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.text.Decoration;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import xyz.nucleoid.plasmid.Plasmid;

import java.util.Optional;

public final class PlasmidMessageTypes {
    public static final RegistryKey<MessageType> TEAM_CHAT = RegistryKey.of(Registry.MESSAGE_TYPE_KEY, new Identifier(Plasmid.ID, "team_chat"));

    public static void register() {
        BuiltinRegistries.add(BuiltinRegistries.MESSAGE_TYPE, TEAM_CHAT, new MessageType(
                Optional.of(MessageType.DisplayRule.of(Decoration.ofTeamMessage("text.plasmid.chat.team"))),
                Optional.empty(),
                Optional.of(MessageType.NarrationRule.of(Decoration.ofChat("chat.type.text.narrate"), MessageType.NarrationRule.Kind.CHAT))
        ));
    }
}
