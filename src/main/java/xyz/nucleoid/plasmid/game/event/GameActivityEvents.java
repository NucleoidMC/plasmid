package xyz.nucleoid.plasmid.game.event;

import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.function.Consumer;

/**
 * Events relating to the lifecycle of a {@link GameActivity} within a {@link GameSpace}.
 */
public final class GameActivityEvents {
    /**
     * Called when a {@link GameActivity} is set on a {@link GameSpace} through {@link GameSpace#setActivity(Consumer)}.
     * <p>
     * This event should be used for any start logic needed to be done by a game.
     * <p>
     * This event is called after {@link GameActivityEvents#CREATE} as well as after {@link GamePlayerEvents#ADD} which
     * will have been called for all players in this {@link GameSpace}.
     */
    public static final StimulusEvent<Enable> ENABLE = StimulusEvent.create(Enable.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onEnable();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link GameActivity} should be disabled. This happens when a {@link GameActivity} is replaced by
     * another on a {@link GameSpace} or when a {@link GameSpace} is closed.
     * <p>
     * This event should be used for any closing logic needed to be done by a game.
     * <p>
     * This event is called before {@link GameActivityEvents#DESTROY} as well as before {@link GamePlayerEvents#REMOVE}
     * which will still be called for all players in this {@link GameSpace}.
     */
    public static final StimulusEvent<Disable> DISABLE = StimulusEvent.create(Disable.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDisable();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called immediately when a {@link GameActivity} is created.
     * <p>
     * This event should be used for any early setup logic, but generally {@link GameActivityEvents#ENABLE} is more
     * useful.
     * <p>
     * This event is called before {@link GameActivityEvents#ENABLE} as well as before {@link GamePlayerEvents#ADD}
     * which will still be called for all players in this {@link GameSpace}.
     */
    public static final StimulusEvent<Create> CREATE = StimulusEvent.create(Create.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onCreate();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link GameActivity} is finally destroyed. This can happen as a result of the {@link GameActivity}
     * being replaced or the {@link GameSpace} closing.
     * <p>
     * This event should be used for any final tear-down logic needed to be done by a game.
     * <p>
     * This event is called after {@link GameActivityEvents#DISABLE} as well as after {@link GamePlayerEvents#REMOVE}
     * which will have been called for all players in this {@link GameSpace}.
     */
    public static final StimulusEvent<Destroy> DESTROY = StimulusEvent.create(Destroy.class, ctx -> reason -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDestroy(reason);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called every tick while a {@link GameActivity} is active.
     */
    public static final StimulusEvent<Tick> TICK = StimulusEvent.create(Tick.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onTick();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when the {@code /game start} command is run by a player.
     * <p>
     * This event should be used to run actual starting logic as well as to return any errors if starting is not
     * currently possible.
     */
    public static final StimulusEvent<RequestStart> REQUEST_START = StimulusEvent.create(RequestStart.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onRequestStart();
                if (result != null) {
                    return result;
                }
            }
            return null;
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return GameResult.error(new TranslatableText("text.plasmid.game.start_result.error"));
        }
    });

    public interface Enable {
        void onEnable();
    }

    public interface Disable {
        void onDisable();
    }

    public interface Create {
        void onCreate();
    }

    public interface Destroy {
        void onDestroy(GameCloseReason reason);
    }

    public interface Tick {
        void onTick();
    }

    public interface RequestStart {
        @Nullable
        GameResult onRequestStart();
    }
}
