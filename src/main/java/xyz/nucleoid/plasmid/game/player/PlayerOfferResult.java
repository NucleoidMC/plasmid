package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public sealed interface PlayerOfferResult permits PlayerOfferResult.Pass, PlayerOfferResult.Accept, PlayerOfferResult.Reject {
    Pass PASS = new Pass();

    final class Pass implements PlayerOfferResult {
        private Pass() {
        }
    }

    non-sealed interface Accept extends PlayerOfferResult {
        PlayerOfferResult.Accept thenRun(Consumer<ServerPlayerEntity> consumer);
    }

    non-sealed interface Reject extends PlayerOfferResult {
        Text reason();
    }
}
