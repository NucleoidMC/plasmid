package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.party.PartyManager;

import java.util.Collection;

// TODO: can normal game logic be merged with game channel logic? all access to a game should go through a game channel
//       opening a game via /game open should create a temporary channel for that game: /game join will qualify with an
//       open channel
public final class GamePlayerAccess {
    public static void joinToGame(ServerPlayerEntity player, ManagedGameSpace gameSpace) {
        PartyManager partyManager = PartyManager.get(player.server);
        Collection<ServerPlayerEntity> players = partyManager.getPartyMembers(player);

        for (ServerPlayerEntity member : players) {
            joinIndividualToGame(member, gameSpace);
        }
    }

    private static void joinIndividualToGame(ServerPlayerEntity player, ManagedGameSpace gameSpace) {
        gameSpace.offerPlayer(player).thenAccept(joinResult -> {
            if (joinResult.isError()) {
                Text error = joinResult.getError();
                player.sendMessage(error.shallowCopy().formatted(Formatting.RED), false);
            }
        });
    }
}
