package xyz.nucleoid.plasmid.api.game.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.player.RespawnResult;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameComponents;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Events relating to players being added and removed from a {@link GameSpace} or {@link GameActivity}.
 */
public final class GamePlayerEvents {
    /**
     * Called when a {@link ServerPlayer} is added to a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayer} should be tracked by a {@link GameActivity}, and is NOT limited to a player
     * specifically joining.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayer} intentionally joins this {@link GameSpace}</li>
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
     * Called when a {@link ServerPlayer} is removed from a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayer} should be no longer be tracked by a {@link GameActivity}, and is NOT limited to a
     * player specifically leaving the game.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayer} intentionally leaves this {@link GameSpace}</li>
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
     * Called when a {@link ServerPlayer} intentionally joins a {@link GameSpace}.
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
     * Called when a {@link ServerPlayer} intentionally leaves a {@link GameSpace} or leaves the server entirely.
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
     * Called when a group of {@link ServerPlayer} tries to join this game.
     * <p>
     * Games should respond to this event in order for players to prevent players from joining or skip any further listeners.
     * {@link JoinOffer#accept()} or {@link JoinOffer#reject(Component)}.
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
            return offer.reject(GameComponents.Join.unexpectedError());
        }
    });

    /**
     * Called when a group of {@link ServerPlayer} is accepted to join this game. This event is responsible for bringing
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
     * Called when display name of {@link ServerPlayer} is created.
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
     * Called when join message of {@link ServerPlayer} is created.
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
     * Called when leave message of {@link ServerPlayer} is created.
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

    /**
     * Called when a player is about to respawn.
     * This event is invoked before the new {@link ServerPlayer} is created.
     * <p>
     * Listeners should return {@link RespawnResult.Respawn} with the target world to keep the player in the game.
     * Otherwise, the player will be thrown out of the game.
     */
    public static final StimulusEvent<RequestRespawn> REQUEST_RESPAWN = StimulusEvent.create(RequestRespawn.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onRequestRespawn(player);
                if (!(result instanceof RespawnResult.Pass)) {
                    return result;
                }
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
        return RespawnResult.PASS;
    });

    /**
     * Called after the new player entity has been created and assigned to the correct world.
     * <p>
     * Should be used to make the player ready (save the new reference, set inventory...).
     */
    public static final StimulusEvent<Respawn> RESPAWN = StimulusEvent.create(Respawn.class, ctx -> (oldPlayer, respawnedPlayer, alive) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRespawn(oldPlayer, respawnedPlayer, alive);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public interface Add {
        void onAddPlayer(ServerPlayer player);
    }

    public interface Remove {
        void onRemovePlayer(ServerPlayer player);
    }

    public interface Offer {
        JoinOfferResult onOfferPlayers(JoinOffer offer);
    }

    public interface Accept {
        JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor);
    }

    public interface Name {
        Component onDisplayNameCreation(ServerPlayer player, Component currentText, Component vanillaText);
    }

    public interface JoinMessage {
        @Nullable
        Component onJoinMessageCreation(ServerPlayer player, @Nullable Component currentText, Component defaultText);
    }

    public interface LeaveMessage {
        @Nullable
        Component onLeaveMessageCreation(ServerPlayer player, @Nullable Component currentText, Component defaultText);
    }

    public interface RequestRespawn {
        RespawnResult onRequestRespawn(ServerPlayer player);
    }

    public interface Respawn {
        void onRespawn(ServerPlayer oldPlayer, ServerPlayer respawnedPlayer, boolean alive);
    }
}
