package xyz.nucleoid.plasmid.game.player;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.ListedGameSpace;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for joining players to a {@link GameSpace}. This handles all logic such as collecting all party
 * members, screening, and offering players to the {@link GameSpace}.
 */
public final class GamePlayerJoiner {
    public static Results tryJoin(ServerPlayerEntity player, ListedGameSpace gameSpace) {
        try {
            var players = collectPlayersForJoin(player, gameSpace);
            return tryJoinAll(players, gameSpace);
        } catch (Throwable throwable) {
            return handleJoinException(throwable);
        }
    }

    private static Set<ServerPlayerEntity> collectPlayersForJoin(ServerPlayerEntity player, ListedGameSpace gameSpace) {
        Set<ServerPlayerEntity> players = new ReferenceOpenHashSet<>();
        players.add(player);

        // TODO: We should be able to collect players within a party even in a proxied setup
        if (gameSpace instanceof GameSpace localGameSpace) {
            GameEvents.COLLECT_PLAYERS_FOR_JOIN.invoker().collectPlayersForJoin(localGameSpace, player, players);
        }

        return players;
    }

    private static Results tryJoinAll(Collection<ServerPlayerEntity> players, ListedGameSpace gameSpace) {
        var results = new Results();

        var screenResult = gameSpace.getPlayers().screenJoins(players);
        if (screenResult.isError()) {
            results.globalError = screenResult.error();
            return results;
        }

        for (var player : players) {
            var result = gameSpace.getPlayers().offer(player);
            if (result.isError()) {
                results.playerErrors.put(player, result.error());
            }
        }

        return results;
    }

    public static Results handleJoinException(Throwable throwable) {
        var results = new Results();
        results.globalError = getFeedbackForException(throwable);
        return results;
    }

    private static Text getFeedbackForException(Throwable throwable) {
        var gameOpenException = GameOpenException.unwrap(throwable);
        if (gameOpenException != null) {
            return gameOpenException.getReason().copy();
        } else {
            return GameTexts.Join.unexpectedError();
        }
    }

    public static final class Results {
        public Text globalError;
        public final Map<ServerPlayerEntity, Text> playerErrors = new Reference2ObjectOpenHashMap<>();

        public void sendErrorsTo(ServerPlayerEntity player) {
            if (this.globalError != null) {
                player.sendMessage(this.globalError.copy().formatted(Formatting.RED), false);
            } else if (!this.playerErrors.isEmpty()) {
                player.sendMessage(
                        GameTexts.Join.partyJoinError(this.playerErrors.size()).formatted(Formatting.RED),
                        false
                );

                for (var entry : this.playerErrors.entrySet()) {
                    Text error = entry.getValue().copy().formatted(Formatting.RED);
                    entry.getKey().sendMessage(error, false);
                }
            }
        }
    }
}
