package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.chat.translation.TranslationHandler;

import java.util.UUID;

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
