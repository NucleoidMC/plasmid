package xyz.nucleoid.plasmid.game.common.team;

import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

public final class TeamChat {
    private final GameSpace gameSpace;

    private TeamChat(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    public static void applyTo(GameActivity activity) {
        TeamChat teamChat = new TeamChat(activity.getGameSpace());
        activity.listen(PlayerChatEvent.EVENT, teamChat::onSendMessage);
    }

    private ActionResult onSendMessage(ServerPlayerEntity sender, Text message) {
        if (this.shouldUseTeamChat(sender)) {
            this.sendTeamChat(sender, message);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private boolean shouldUseTeamChat(ServerPlayerEntity player) {
        if (player.getScoreboardTeam() == null) {
            return false;
        }

        if (player instanceof HasChatChannel) {
            return ((HasChatChannel) player).getChatChannel() == ChatChannel.TEAM;
        } else {
            return false;
        }
    }

    private void sendTeamChat(ServerPlayerEntity sender, Text message) {
        Team team = (Team) sender.getScoreboardTeam();
        Text teamMessage = new TranslatableText("text.plasmid.chat.team", message);

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            if (player == sender || player.getScoreboardTeam() == team) {
                player.sendSystemMessage(teamMessage, sender.getUuid());
            }
        }
    }
}
