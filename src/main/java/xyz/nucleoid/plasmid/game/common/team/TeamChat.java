package xyz.nucleoid.plasmid.game.common.team;

import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;
import xyz.nucleoid.plasmid.chat.PlasmidMessageTypes;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

public final class TeamChat {
    private final GameSpace gameSpace;
    private final TeamManager manager;

    private TeamChat(GameSpace gameSpace, TeamManager manager) {
        this.gameSpace = gameSpace;
        this.manager = manager;
    }

    public static void addTo(GameActivity activity, TeamManager manager) {
        var teamChat = new TeamChat(activity.getGameSpace(), manager);
        activity.listen(PlayerChatEvent.EVENT, teamChat::onSendMessage);
    }

    private ActionResult onSendMessage(MessageSender sender, SignedMessage message) {
        final ServerPlayerEntity player = this.gameSpace.getPlayers().getEntity(sender.uuid());
        if (player == null) {
            return ActionResult.PASS;
        }

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
