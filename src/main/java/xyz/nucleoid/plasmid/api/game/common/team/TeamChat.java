package xyz.nucleoid.plasmid.api.game.common.team;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.chat.ChatChannel;
import xyz.nucleoid.plasmid.api.chat.HasChatChannel;
import xyz.nucleoid.plasmid.api.chat.PlasmidMessageTypes;
import xyz.nucleoid.plasmid.api.game.GameActivity;
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

    private boolean onSendMessage(ServerPlayer player, PlayerChatMessage message, ChatType.Bound messageType) {
        var team = this.manager.teamFor(player);

        if (team != null && player instanceof HasChatChannel hasChannel && hasChannel.getChatChannel() == ChatChannel.TEAM) {
            var teamName = this.manager.getTeamConfig(team).name();
            var teamMessageType = ChatType.bind(PlasmidMessageTypes.TEAM_CHAT, player).withTargetName(teamName);

            var sentMessage = OutgoingChatMessage.create(message);
            for (var receiver : this.manager.playersIn(team)) {
                receiver.sendChatMessage(sentMessage, player.shouldFilterMessageTo(receiver), teamMessageType);
            }

            return true;
        }

        return false;
    }
}
