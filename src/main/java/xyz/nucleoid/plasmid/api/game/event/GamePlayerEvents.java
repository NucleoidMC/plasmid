package xyz.nucleoid.plasmid.api.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameTexts;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Events relating to players being added and removed from a {@link GameSpace} or {@link GameActivity}.
 */
public final class GamePlayerEvents {
    /**
     * Called when a {@link ServerPlayerEntity} is added to a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayerEntity} should be tracked by a {@link GameActivity}, and is NOT limited to a player
     * specifically joining.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayerEntity} intentionally joins this {@link GameSpace}</li>
     * <li>A new {@link GameActivity} is created, and all players are transferred</li>
     * <p>
     * This event will always be fired after {@link GameActivityEvents#CREATE} and before {@link GameActivityEvents#ENABLE}.
     * <p>
     * This event is invoked after the player has been added to the {@link GameSpace#getPlayers() game player set}.
     *
     * @see GamePlayerEvents#JOIN
     */
    public static final StimulusEvent<Add> ADD = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} is removed from a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayerEntity} should be no longer be tracked by a {@link GameActivity}, and is NOT limited to a
     * player specifically leaving the game.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayerEntity} intentionally leaves this {@link GameSpace}</li>
     * <li>A {@link GameSpace} is closed or {@link GameActivity} replaced</li>
     * <p>
     * This event will always be fired before {@link GameActivityEvents#DESTROY} and after {@link GameActivityEvents#DISABLE}.
     * <p>
     * This event is invoked before the player is removed from the {@link GameSpace#getPlayers() game player set}.
     *
     * @see GamePlayerEvents#LEAVE
     */
    public static final StimulusEvent<Remove> REMOVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} intentionally joins a {@link GameSpace}.
     *
     * @see GamePlayerEvents#ADD
     * @see GamePlayerEvents#OFFER
     */
    public static final StimulusEvent<Add> JOIN = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} intentionally leaves a {@link GameSpace} or leaves the server entirely.
     *
     * @see GamePlayerEvents#REMOVE
     */
    public static final StimulusEvent<Remove> LEAVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a group of {@link ServerPlayerEntity} tries to join this game.
     * <p>
     * Games should respond to this event in order for players to prevent players from joining or skip any further listeners.
     * {@link JoinOffer#accept()} or {@link JoinOffer#reject(Text)}.
     *
     * @see JoinOffer
     * @see JoinOfferResult
     * @see GamePlayerEvents#ACCEPT
     */
    public static final StimulusEvent<Offer> OFFER = StimulusEvent.create(Offer.class, ctx -> offer -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onOfferPlayers(offer);
                if (!(result instanceof JoinOfferResult.Pass)) {
                    return result;
                }
            }
            return offer.accept();
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return offer.reject(GameTexts.Join.unexpectedError());
        }
    });

    /**
     * Called when a group of {@link ServerPlayerEntity} is accepted to join this game. This event is responsible for bringing
     * the players into the {@link GameSpace} world in the correct location.
     * <p>
     * Games must respond to this event in order for players to be able to join.
     *
     * @see JoinAcceptor
     * @see JoinAcceptorResult
     * @see GamePlayerEvents#JOIN
     */
    public static final StimulusEvent<Accept> ACCEPT = StimulusEvent.create(Accept.class, ctx -> accept -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onAcceptPlayers(accept);
                if (!(result instanceof JoinAcceptorResult.Pass)) {
                    return result;
                }
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
        return accept.pass();
    });

    /**
     * Called when display name of {@link ServerPlayerEntity} is created.
     * Can be used to manipulate it in game.
     */
    public static final StimulusEvent<Name> DISPLAY_NAME = StimulusEvent.create(Name.class, ctx -> (player, current, vanillaText) -> {
        try {
            for (var listener : ctx.getListeners()) {
                current = listener.onDisplayNameCreation(player, current, vanillaText);
            }
            return current;
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return vanillaText;
        }
    });

    /**
     * Called when join message of {@link ServerPlayerEntity} is created.
     * Can be used to manipulate it in game.
     * This event is invoked after game handles player being added, but before the global join event
     *
     * Event returns a Text to set it or {@code null} to disable it.
     */
    public static final StimulusEvent<JoinMessage> JOIN_MESSAGE = StimulusEvent.create(JoinMessage.class, ctx -> (player, current, defaultText) -> {
        try {
            for (var listener : ctx.getListeners()) {
                current = listener.onJoinMessageCreation(player, current, defaultText);
            }
            return current;
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return defaultText;
        }
    });

    /**
     * Called when leave message of {@link ServerPlayerEntity} is created.
     * Can be used to manipulate it in game.
     * This event is invoked before game handles player being removed

     * Event returns a Text to set it or {@code null} to disable it.
     */
    public static final StimulusEvent<LeaveMessage> LEAVE_MESSAGE = StimulusEvent.create(LeaveMessage.class, ctx -> (player, current, defaultText) -> {
        try {
            for (var listener : ctx.getListeners()) {
                current = listener.onLeaveMessageCreation(player, current, defaultText);
            }
            return current;
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return defaultText;
        }
    });

    public interface Add {
        void onAddPlayer(ServerPlayerEntity player);
    }

    public interface Remove {
        void onRemovePlayer(ServerPlayerEntity player);
    }

    public interface Offer {
        JoinOfferResult onOfferPlayers(JoinOffer offer);
    }

    public interface Accept {
        JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor);
    }

    public interface Name {
        Text onDisplayNameCreation(ServerPlayerEntity player, Text currentText, Text vanillaText);
    }

    public interface JoinMessage {
        @Nullable
        Text onJoinMessageCreation(ServerPlayerEntity player, @Nullable Text currentText, Text defaultText);
    }

    public interface LeaveMessage {
        @Nullable
        Text onLeaveMessageCreation(ServerPlayerEntity player, @Nullable Text currentText, Text defaultText);
    }
}
