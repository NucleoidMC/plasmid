package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public sealed interface JoinOfferResult permits JoinOfferResult.Pass, JoinOfferResult.Accept, JoinOfferResult.Reject {
    Pass PASS = new Pass();

    final class Pass implements JoinOfferResult {
        private Pass() {
        }
    }

    non-sealed interface Accept extends JoinOfferResult {
        JoinOfferResult.Accept thenRun(Consumer<PlayerSet> consumer);

        default JoinOfferResult.Accept thenRunForEach(Consumer<ServerPlayerEntity> consumer) {
            return this.thenRun(players -> players.forEach(consumer));
        }
    }

    non-sealed interface Reject extends JoinOfferResult {
        Text reason();
    }
}
