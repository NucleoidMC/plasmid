package xyz.nucleoid.plasmid.chat;

import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

import java.util.UUID;

public final class ChatChannelManager {
    public static void registerCallbacks() {
        Stimuli.global().listen(PlayerChatEvent.EVENT, (sender, message) -> {
            ChatChannel chatChannel = ((HasChatChannel) sender).getChatChannel();
            if (chatChannel == ChatChannel.TEAM && isTeamChatAllowed(sender)) {
                sendTeamChat(message, sender);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private static boolean isTeamChatAllowed(ServerPlayerEntity sender) {
        if (sender.getScoreboardTeam() == null) {
            return false;
        }

        ManagedGameSpace gameSpace = GameSpaceManager.get().byPlayer(sender);
        return gameSpace != null && gameSpace.getBehavior().testRule(GameRule.TEAM_CHAT) == ActionResult.SUCCESS;
    }

    private static void sendTeamChat(Text message, ServerPlayerEntity sender) {
        Team team = (Team) sender.getScoreboardTeam();
        UUID senderUuid = sender.getUuid();

        Text teamMessage = new TranslatableText("text.plasmid.chat.team", message);

        for (ServerPlayerEntity player : sender.server.getPlayerManager().getPlayerList()) {
            if (player == sender || player.getScoreboardTeam() == team) {
                player.sendSystemMessage(teamMessage, senderUuid);
            }
        }
    }
}
