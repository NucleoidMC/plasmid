package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public sealed interface PlayerOfferResult permits PlayerOfferResult.Pass, PlayerOfferResult.Accept, PlayerOfferResult.Reject {
    Pass PASS = new Pass();

    final class Pass implements PlayerOfferResult {
        private Pass() {
        }
    }

    non-sealed interface Accept extends PlayerOfferResult {
        PlayerOfferResult.Accept thenRun(Consumer<PlayerSet> consumer);

        default PlayerOfferResult.Accept thenRunForEach(Consumer<ServerPlayerEntity> consumer) {
            return this.thenRun(players -> players.forEach(consumer));
        }
    }

    non-sealed interface Reject extends PlayerOfferResult {
        Text reason();
    }
}
