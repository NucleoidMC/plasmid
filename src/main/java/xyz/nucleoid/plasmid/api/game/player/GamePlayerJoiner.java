package xyz.nucleoid.plasmid.api.game.player;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.event.GameEvents;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.Plasmid;

import java.util.Collection;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class for joining players to a {@link GameSpace}. This handles all logic such as collecting all party
 * members, and offering players to the {@link GameSpace}.
 */
public final class GamePlayerJoiner {
    public static GameResult tryJoin(ServerPlayer player, GameSpace gameSpace, JoinIntent intent) {
        try {
            var players = collectPlayersForJoin(player, gameSpace);
            return tryJoinAll(players, gameSpace, intent);
        } catch (Throwable throwable) {
            return handleJoinException(throwable);
        }
    }

    private static Set<ServerPlayer> collectPlayersForJoin(ServerPlayer player, GameSpace gameSpace) {
        Set<ServerPlayer> players = new ReferenceOpenHashSet<>();
        players.add(player);

        GameEvents.COLLECT_PLAYERS_FOR_JOIN.invoker().collectPlayersForJoin(gameSpace, player, players);

        return players;
    }

    private static GameResult tryJoinAll(Collection<ServerPlayer> players, GameSpace gameSpace, JoinIntent intent) {
        boolean playersAreAllowed = true;
        for (ServerPlayer player : players) {
            if (!gameSpace.isPlayerAllowed(PlayerRef.of(player))) {
                playersAreAllowed = false;
                break;
            }
        }

        if (!playersAreAllowed) {
            return GameResult.error(Component.translatable("text.plasmid.game.join.party.error.not_allowed"));
        }
        return gameSpace.getPlayers().offer(players, intent);
    }

    public static GameResult handleJoinException(Throwable throwable) {
        Plasmid.LOGGER.error("Failed to add player to game space!", throwable);
        return GameResult.error(getFeedbackForException(throwable));
    }

    private static Component getFeedbackForException(Throwable throwable) {
        var gameOpenException = GameOpenException.unwrap(throwable);
        if (gameOpenException != null) {
            return gameOpenException.getReason().copy();
        } else {
            return GameComponents.Join.unexpectedError();
        }
    }
}
