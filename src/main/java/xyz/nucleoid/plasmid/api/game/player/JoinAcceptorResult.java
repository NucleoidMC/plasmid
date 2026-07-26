package xyz.nucleoid.plasmid.api.game.player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

public sealed interface JoinAcceptorResult permits JoinAcceptorResult.Pass, JoinAcceptorResult.Teleport {
    Pass PASS = new Pass();

    final class Pass implements JoinAcceptorResult {
        private Pass() {
        }
    }

    non-sealed interface Teleport extends JoinAcceptorResult {
        Teleport thenRun(BiConsumer<PlayerSet, JoinIntent> consumer);

        default Teleport thenRun(Consumer<PlayerSet> consumer) {
            return this.thenRun((players, intent) -> consumer.accept(players));
        }

        default Teleport thenRunForEach(Consumer<ServerPlayer> consumer) {
            return this.thenRun((players, intent) -> players.forEach(consumer));
        }

        default Teleport thenRunForEach(BiConsumer<ServerPlayer, JoinIntent> consumer) {
            return this.thenRun((players, intent) -> players.forEach(player -> consumer.accept(player, intent)));
        }
    }
}
