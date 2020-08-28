package xyz.nucleoid.plasmid.mixin.chat;

import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.PlayerChatListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Nullable
    public abstract ServerPlayerEntity getPlayer(UUID uuid);

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayerList();

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        if (type != MessageType.CHAT || senderUuid == Util.NIL_UUID) {
            return;
        }

        ServerPlayerEntity sender = this.getPlayer(senderUuid);
        if (sender == null) {
            return;
        } else {
            GameWorld gameWorld = GameWorld.forWorld(sender.world);
            if (gameWorld != null && gameWorld.containsEntity(sender) && gameWorld.invoker(PlayerChatListener.EVENT).onSendChatMessage(message, sender) == ActionResult.FAIL) {
                ci.cancel();
            }
            if ((((HasChatChannel) sender).getChatChannel()) != ChatChannel.TEAM) return;
        }

        if (this.isTeamChatAllowed(sender)) {
            this.sendTeamChat(message, sender);
            ci.cancel();
        }
    }

    private boolean isTeamChatAllowed(ServerPlayerEntity sender) {
        if (sender.getScoreboardTeam() == null) {
            return false;
        }

        GameWorld gameWorld = GameWorld.forWorld(sender.world);
        return gameWorld != null && gameWorld.testRule(GameRule.TEAM_CHAT) == RuleResult.ALLOW;
    }

    private void sendTeamChat(Text message, ServerPlayerEntity sender) {
        Team team = (Team) sender.getScoreboardTeam();
        UUID senderUuid = sender.getUuid();

        Text teamMessage = new LiteralText("[Team] ").append(message);

        for (ServerPlayerEntity player : this.getPlayerList()) {
            if (player == sender || player.getScoreboardTeam() == team) {
                player.sendSystemMessage(teamMessage, senderUuid);
            }
        }
    }
}
