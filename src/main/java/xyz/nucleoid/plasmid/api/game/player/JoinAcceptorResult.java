package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

        default Teleport thenRunForEach(Consumer<ServerPlayerEntity> consumer) {
            return this.thenRun((players, intent) -> players.forEach(consumer));
        }

        default Teleport thenRunForEach(BiConsumer<ServerPlayerEntity, JoinIntent> consumer) {
            return this.thenRun((players, intent) -> players.forEach(player -> consumer.accept(player, intent)));
        }
    }
}
