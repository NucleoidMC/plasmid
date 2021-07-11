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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for joining players to a {@link GameSpace}. This handles all logic such as collecting all party
 * members, screening, and offering players to the {@link GameSpace}.
 */
public final class GamePlayerJoiner {
    private final GameSpace gameSpace;

    public GamePlayerJoiner(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    public Results tryJoin(ServerPlayerEntity player) {
        try {
            var players = this.collectPlayersForJoin(player, this.gameSpace);
            return this.tryJoinAll(players, this.gameSpace);
        } catch (Throwable throwable) {
            return this.handleJoinException(throwable);
        }
    }

    private Set<ServerPlayerEntity> collectPlayersForJoin(ServerPlayerEntity player, GameSpace gameSpace) {
        Set<ServerPlayerEntity> players = new ReferenceOpenHashSet<>();
        players.add(player);

        GameEvents.COLLECT_PLAYERS_FOR_JOIN.invoker().collectPlayersForJoin(gameSpace, player, players);

        return players;
    }

    private Results tryJoinAll(Collection<ServerPlayerEntity> players, GameSpace gameSpace) {
        var results = new Results();

        var screenResult = gameSpace.screenPlayerJoins(players);
        if (screenResult.isError()) {
            results.globalError = screenResult.error();
            return results;
        }

        for (var player : players) {
            var result = gameSpace.offerPlayer(player);
            if (result.isError()) {
                results.playerErrors.put(player, result.error());
            }
        }

        return results;
    }

    private Results handleJoinException(Throwable throwable) {
        var results = new Results();
        results.globalError = this.getFeedbackForException(throwable);
        return results;
    }

    private Text getFeedbackForException(Throwable throwable) {
        var gameOpenException = GameOpenException.unwrap(throwable);
        if (gameOpenException != null) {
            return gameOpenException.getReason().shallowCopy();
        } else {
            return GameTexts.Join.unexpectedError();
        }
    }

    public static final class Results {
        public Text globalError;
        public final Map<ServerPlayerEntity, Text> playerErrors = new Reference2ObjectOpenHashMap<>();

        public void sendErrorsTo(ServerPlayerEntity player) {
            if (this.globalError != null) {
                player.sendMessage(this.globalError.shallowCopy().formatted(Formatting.RED), false);
            } else if (!this.playerErrors.isEmpty()) {
                player.sendMessage(
                        GameTexts.Join.partyJoinError(this.playerErrors.size()).formatted(Formatting.RED),
                        false
                );

                for (var entry : this.playerErrors.entrySet()) {
                    Text error = entry.getValue().shallowCopy().formatted(Formatting.RED);
                    entry.getKey().sendMessage(error, false);
                }
            }
        }
    }
}
