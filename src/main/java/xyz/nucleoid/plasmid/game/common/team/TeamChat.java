package xyz.nucleoid.plasmid.game.common.team;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.chat.PlasmidMessageTypes;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.player.ReplacePlayerChatEvent;

public final class TeamChat {
    private final TeamManager manager;

    private TeamChat(TeamManager manager) {
        this.manager = manager;
    }

    public static void addTo(GameActivity activity, TeamManager manager) {
        var teamChat = new TeamChat(manager);
        activity.listen(ReplacePlayerChatEvent.EVENT, teamChat::onSendMessage);
    }

    private boolean onSendMessage(ServerPlayerEntity player, SignedMessage message, MessageType.Parameters messageType) {
        var team = this.manager.teamFor(player);

        if (team != null && player instanceof HasChatChannel hasChannel && hasChannel.getChatChannel() == ChatChannel.TEAM) {
            var teamName = this.manager.getTeamConfig(team).name();
            var teamMessageType = MessageType.params(PlasmidMessageTypes.TEAM_CHAT, player).withTargetName(teamName);

            var sentMessage = SentMessage.of(message);
            for (var receiver : this.manager.playersIn(team)) {
                receiver.sendChatMessage(sentMessage, player.shouldFilterMessagesSentTo(receiver), teamMessageType);
            }
            sentMessage.afterPacketsSent(player.server.getPlayerManager());

            return true;
        }

        return false;
    }
}
