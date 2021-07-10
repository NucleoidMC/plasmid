package xyz.nucleoid.plasmid.game.event;

import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public final class GameActivityEvents {
    public static final StimulusEvent<Enable> ENABLE = StimulusEvent.create(Enable.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onEnable();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Disable> DISABLE = StimulusEvent.create(Disable.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDisable();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Create> CREATE = StimulusEvent.create(Create.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onCreate();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Destroy> DESTROY = StimulusEvent.create(Destroy.class, ctx -> reason -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDestroy(reason);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    public static final StimulusEvent<Tick> TICK = StimulusEvent.create(Tick.class, ctx -> () -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onTick();
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

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
