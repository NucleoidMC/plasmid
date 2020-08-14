package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.entity.Entity;
import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorld;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Shadow @Nullable public abstract ServerPlayerEntity getPlayer(UUID uuid);

    @Shadow public abstract List<ServerPlayerEntity> getPlayerList();

    private static final Style STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        ServerPlayerEntity sender = this.getPlayer(senderUuid);

        if (sender == null || (((HasChatChannel) sender).getChatChannel()) == ChatChannel.ALL) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(sender.world);
        Team team = (Team) sender.getScoreboardTeam();

        if (team == null || gameWorld == null || gameWorld.testRule(GameRule.TEAM_CHAT) != RuleResult.ALLOW) {
            return;
        }

        Text text = team.getFormattedName().fillStyle(STYLE);

        for (ServerPlayerEntity curPlayer : this.getPlayerList()) {
            if (curPlayer == sender) {
                curPlayer.sendSystemMessage(new TranslatableText("chat.type.team.sent", text, sender.getDisplayName(), message), sender.getUuid());
            } else if (curPlayer.getScoreboardTeam() == team) {
                curPlayer.sendSystemMessage(new TranslatableText("chat.type.team.text", text, sender.getDisplayName(), message), sender.getUuid());
            }
        }

        ci.cancel();
    }
}
