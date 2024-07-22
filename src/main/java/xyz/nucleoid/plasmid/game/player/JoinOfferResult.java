package xyz.nucleoid.plasmid.game.player;

import net.minecraft.text.Text;

public sealed interface JoinOfferResult permits JoinOfferResult.Pass, JoinOfferResult.Accept, JoinOfferResult.Reject {
    Pass PASS = new Pass();
    Accept ACCEPT = new Accept();

    final class Pass implements JoinOfferResult {
        private Pass() {
        }
    }

    final class Accept implements JoinOfferResult {
        private Accept() {
        }
    }

    non-sealed interface Reject extends JoinOfferResult {
        Text reason();
    }
}
