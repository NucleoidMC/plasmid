package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements HasChatChannel {
    public ChatChannel chatChannel = ChatChannel.TEAM;

    @Override
    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    @Override
    public void setChatChannel(ChatChannel channel) {
        this.chatChannel = channel;
    }
}
