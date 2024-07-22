package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public sealed interface JoinAcceptorResult permits JoinAcceptorResult.Pass, JoinAcceptorResult.Teleport {
    Pass PASS = new Pass();

    final class Pass implements JoinAcceptorResult {
        private Pass() {
        }
    }

    non-sealed interface Teleport extends JoinAcceptorResult {
        Teleport thenRun(Consumer<PlayerSet> consumer);

        default Teleport thenRunForEach(Consumer<ServerPlayerEntity> consumer) {
            return this.thenRun(players -> players.forEach(consumer));
        }
    }
}
