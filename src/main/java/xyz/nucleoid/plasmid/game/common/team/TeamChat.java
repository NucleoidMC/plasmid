package xyz.nucleoid.plasmid.game.common.team;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

public final class TeamChat {
    private final TeamManager manager;

    private TeamChat(TeamManager manager) {
        this.manager = manager;
    }

    public static void applyTo(GameActivity activity, TeamManager manager) {
        var teamChat = new TeamChat(manager);
        activity.listen(PlayerChatEvent.EVENT, teamChat::onSendMessage);
    }

    private ActionResult onSendMessage(ServerPlayerEntity sender, Text message) {
        var team = this.manager.teamFor(sender);

        if (team != null && sender instanceof HasChatChannel hasChannel && hasChannel.getChatChannel() == ChatChannel.TEAM) {
            var teamMessage = new TranslatableText("text.plasmid.chat.team", message);

            for (var player : this.manager.playersIn(team)) {
                player.sendSystemMessage(teamMessage, sender.getUuid());
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

}
