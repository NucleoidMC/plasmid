package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collection;

public final class GamePlayerEvents {
    public static final StimulusEvent<Add> ADD = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (Add listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Remove> REMOVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (Remove listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Add> JOIN = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (Add listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Remove> LEAVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (Remove listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<ScreenJoins> SCREEN_JOINS = StimulusEvent.create(ScreenJoins.class, ctx -> players -> {
        try {
            for (ScreenJoins listener : ctx.getListeners()) {
                GameResult result = listener.screenJoins(players);
                if (result.isError()) {
                    return result;
                }
            }
            return GameResult.ok();
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return GameResult.error(GameTexts.Join.unexpectedError());
        }
    });

    public static final StimulusEvent<Offer> OFFER = StimulusEvent.create(Offer.class, ctx -> offer -> {
        try {
            for (Offer listener : ctx.getListeners()) {
                PlayerOfferResult result = listener.onOfferPlayer(offer);
                if (result.isTerminal()) {
                    return result;
                }
            }
            return offer.pass();
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return offer.reject(GameTexts.Join.unexpectedError());
        }
    });

    public interface Add {
        void onAddPlayer(ServerPlayerEntity player);
    }

    public interface Remove {
        void onRemovePlayer(ServerPlayerEntity player);
    }

    public interface ScreenJoins {
        GameResult screenJoins(Collection<ServerPlayerEntity> players);
    }

    public interface Offer {
        PlayerOfferResult onOfferPlayer(PlayerOffer offer);
    }
}
