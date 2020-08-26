package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.translation.TranslationHandler;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin {

    @Shadow @Final private @Nullable Entity entity;

    @Inject(method = "sendFeedback", at = @At("HEAD"))
    private void sendCorrectFeedback(Text message, boolean broadcastToOps, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayerEntity && message instanceof TranslatableText)
            message = TranslationHandler.getCorrectText((TranslatableText)message, (ServerPlayerEntity)this.entity);
    }
}
