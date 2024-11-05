package xyz.nucleoid.plasmid.game.player;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;

import java.util.Collection;
import java.util.Set;

/**
 * Utility class for joining players to a {@link GameSpace}. This handles all logic such as collecting all party
 * members, and offering players to the {@link GameSpace}.
 */
public final class GamePlayerJoiner {
    public static GameResult tryJoin(ServerPlayerEntity player, GameSpace gameSpace, JoinIntent intent) {
        try {
            var players = collectPlayersForJoin(player, gameSpace);
            return tryJoinAll(players, gameSpace, intent);
        } catch (Throwable throwable) {
            return handleJoinException(throwable);
        }
    }

    private static Set<ServerPlayerEntity> collectPlayersForJoin(ServerPlayerEntity player, GameSpace gameSpace) {
        Set<ServerPlayerEntity> players = new ReferenceOpenHashSet<>();
        players.add(player);

        GameEvents.COLLECT_PLAYERS_FOR_JOIN.invoker().collectPlayersForJoin(gameSpace, player, players);

        return players;
    }

    private static GameResult tryJoinAll(Collection<ServerPlayerEntity> players, GameSpace gameSpace, JoinIntent intent) {
        return gameSpace.getPlayers().offer(players, intent);
    }

    public static GameResult handleJoinException(Throwable throwable) {
        return GameResult.error(getFeedbackForException(throwable));
    }

    private static Text getFeedbackForException(Throwable throwable) {
        var gameOpenException = GameOpenException.unwrap(throwable);
        if (gameOpenException != null) {
            return gameOpenException.getReason().copy();
        } else {
            return GameTexts.Join.unexpectedError();
        }
    }
}
