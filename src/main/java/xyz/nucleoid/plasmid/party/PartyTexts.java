package xyz.nucleoid.plasmid.party;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.game.GameTexts;

public final class PartyTexts {
    public static MutableText displayError(PartyError error, ServerPlayerEntity player) {
        return displayError(error, player.getGameProfile().getName());
    }

    public static MutableText displayError(PartyError error, String player) {
        return switch (error) {
            case DOES_NOT_EXIST -> new TranslatableText("text.plasmid.party.error.does_not_exist");
            case ALREADY_INVITED -> new TranslatableText("text.plasmid.party.error.already_invited", player);
            case ALREADY_IN_PARTY -> new TranslatableText("text.plasmid.party.error.already_in_party");
            case CANNOT_REMOVE_SELF -> new TranslatableText("text.plasmid.party.error.cannot_remove_self");
            case NOT_IN_PARTY -> new TranslatableText("text.plasmid.party.error.not_in_party", player);
            case NOT_INVITED -> new TranslatableText("text.plasmid.party.error.not_invited");
        };
    }

    public static MutableText joinSuccess(ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.join.success", player.getDisplayName());
    }

    public static MutableText leaveSuccess(ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.leave.success", player.getDisplayName());
    }

    public static MutableText disbandSuccess() {
        return new TranslatableText("text.plasmid.party.disband.success");
    }

    public static MutableText transferredSender(ServerPlayerEntity transferredTo) {
        return new TranslatableText("text.plasmid.party.transferred.sender", transferredTo.getDisplayName());
    }

    public static MutableText transferredReceiver(ServerPlayerEntity transferredFrom) {
        return new TranslatableText("text.plasmid.party.transferred.receiver", transferredFrom.getDisplayName());
    }

    public static MutableText kickedSender(ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.kicked.sender", player.getDisplayName());
    }

    public static MutableText kickedReceiver() {
        return new TranslatableText("text.plasmid.party.kicked.receiver");
    }

    public static MutableText invitedSender(ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.invited.sender", player.getDisplayName());
    }

    public static MutableText invitedReceiver(ServerPlayerEntity owner, ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.invited.receiver", owner.getDisplayName())
                .append(PartyTexts.inviteNotificationLink(owner, player));
    }

    public static MutableText inviteNotificationLink(ServerPlayerEntity owner, ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.invited.receiver.click")
                .setStyle(
                        GameTexts.commandLinkStyle(
                                "/party accept " + owner.getGameProfile().getName(),
                                new TranslatableText("text.plasmid.party.invited.receiver.hover", player.getDisplayName())
                        )
                );
    }

    public static MutableText leftGame(ServerPlayerEntity player) {
        return new TranslatableText("text.plasmid.party.left_game", player.getDisplayName());
    }
}
