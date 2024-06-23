package xyz.nucleoid.plasmid.game.player;

import net.minecraft.text.Text;

public sealed interface PlayerOfferResult permits PlayerOfferResult.Pass, PlayerOfferResult.Accept, PlayerOfferResult.Reject {
    Pass PASS = new Pass();

    final class Pass implements PlayerOfferResult {
        private Pass() {
        }
    }

    non-sealed interface Accept extends PlayerOfferResult {
        PlayerOfferResult.Accept and(Runnable and);
    }

    non-sealed interface Reject extends PlayerOfferResult {
        Text reason();
    }
}
