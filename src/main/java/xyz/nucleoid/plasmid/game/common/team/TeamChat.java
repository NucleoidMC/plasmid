package xyz.nucleoid.plasmid.game.common.team;

import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.chat.PlasmidMessageTypes;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

public final class TeamChat {
    private final TeamManager manager;

    private TeamChat(TeamManager manager) {
        this.manager = manager;
    }

    public static void addTo(GameActivity activity, TeamManager manager) {
        var teamChat = new TeamChat(manager);
        activity.listen(PlayerChatEvent.EVENT, teamChat::onSendMessage);
    }

    private ActionResult onSendMessage(ServerPlayerEntity player, MessageSender sender, SignedMessage message) {
        var team = this.manager.teamFor(player);

        if (team != null && player instanceof HasChatChannel hasChannel && hasChannel.getChatChannel() == ChatChannel.TEAM) {
            for (var receiver : this.manager.playersIn(team)) {
                receiver.sendChatMessage(message, sender, PlasmidMessageTypes.TEAM_CHAT);
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

}
