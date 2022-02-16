package xyz.nucleoid.plasmid.game.manager;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameBehavior;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collections;
import java.util.function.Supplier;

final class GameActivityState implements GameBehavior {
    private final GameSpace gameSpace;
    private ManagedGameActivity activity;

    GameActivityState(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    void setActivity(Supplier<ManagedGameActivity> factory) {
        var gameSpace = this.gameSpace;

        var closedActivity = this.activity;
        if (closedActivity != null) {
            disableActivity(gameSpace, closedActivity);
            destroyActivity(gameSpace, closedActivity, GameCloseReason.SWAPPED);
        }

        var activity = factory.get();
        this.activity = activity;

        createActivity(gameSpace, activity);
        enableActivity(gameSpace, activity);

        this.gameSpace.getLifecycle().onActivityChange(gameSpace, activity, closedActivity);
    }

    boolean closeActivity(GameCloseReason reason) {
        var activity = this.activity;
        if (activity == null) {
            return false;
        }

        this.activity = null;

        var gameSpace = this.gameSpace;
        disableActivity(gameSpace, activity);
        destroyActivity(gameSpace, activity, reason);

        return true;
    }

    private static void createActivity(GameSpace gameSpace, ManagedGameActivity activity) {
        activity.propagatingInvoker(GameActivityEvents.CREATE).onCreate();

        GameEvents.CREATE_ACTIVITY.invoker().onCreateActivity(gameSpace, activity);
    }

    private static void enableActivity(GameSpace gameSpace, ManagedGameActivity activity) {
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            activity.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);
        }

        activity.propagatingInvoker(GameActivityEvents.ENABLE).onEnable();
    }

    private static void disableActivity(GameSpace gameSpace, ManagedGameActivity activity) {
        activity.invoker(GameActivityEvents.DISABLE).onDisable();
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            activity.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);
        }
    }

    private static void destroyActivity(GameSpace gameSpace, ManagedGameActivity activity, GameCloseReason reason) {
        activity.invoker(GameActivityEvents.DESTROY).onDestroy(reason);
        activity.onDestroy();

        GameEvents.DESTROY_ACTIVITY.invoker().onDestroyActivity(gameSpace, activity, reason);
    }

    @Override
    @NotNull
    public <T> T invoker(StimulusEvent<T> event) {
        var activity = this.activity;
        return activity != null ? activity.invoker(event) : event.emptyInvoker();
    }

    @Override
    @NotNull
    public <T> T propagatingInvoker(StimulusEvent<T> event) {
        var activity = this.activity;
        return activity != null ? activity.propagatingInvoker(event) : event.emptyInvoker();
    }

    @Override
    @NotNull
    public <T> Iterable<T> getInvokers(StimulusEvent<T> event) {
        var activity = this.activity;
        return activity != null ? activity.getInvokers(event) : Collections.emptyList();
    }

    @Override
    @NotNull
    public ActionResult testRule(GameRuleType rule) {
        var activity = this.activity;
        return activity != null ? activity.testRule(rule) : ActionResult.PASS;
    }
}
