package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameResources;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameEventExceptionHandler;
import xyz.nucleoid.plasmid.game.event.GameEventListeners;
import xyz.nucleoid.plasmid.game.rule.GameRuleMap;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collections;

public final class ManagedGameActivity implements GameActivity {
    private final ManagedGameSpace space;

    private final GameEventListeners listeners;
    private final GameRuleMap rules = new GameRuleMap();
    private final GameResources resources = new GameResources();

    ManagedGameActivity(ManagedGameSpace space) {
        this.space = space;

        var exceptionHandler = createExceptionHandler(space);
        this.listeners = new GameEventListeners(exceptionHandler);
    }

    private static GameEventExceptionHandler createExceptionHandler(ManagedGameSpace space) {
        return new GameEventExceptionHandler() {
            @Override
            public <T> void handleException(StimulusEvent<T> event, Throwable throwable) {
                var listenerName = event.getListenerType().getSimpleName();

                Plasmid.LOGGER.warn("An unexpected exception occurred while invoking {}", listenerName, throwable);
                space.getLifecycle().onError(space, throwable, "Invoking " + listenerName);
            }
        };
    }

    @Override
    public GameSpace getGameSpace() {
        return this.space;
    }

    @Override
    public ManagedGameActivity setRule(GameRuleType rule, EventResult result) {
        this.rules.set(rule, result);
        return this;
    }

    @Override
    public <T> ManagedGameActivity listen(StimulusEvent<T> event, T listener) {
        this.listeners.listen(event, listener);
        return this;
    }

    @Override
    public ManagedGameActivity addResource(AutoCloseable resource) {
        this.resources.add(resource);
        return this;
    }

    @Override
    @NotNull
    public <T> T invoker(StimulusEvent<T> event) {
        return this.listeners.getInvoker(event);
    }

    @Override
    @NotNull
    public <T> T propagatingInvoker(StimulusEvent<T> event) {
        return this.listeners.getPropagatingInvoker(event);
    }

    @Override
    @NotNull
    public <T> Iterable<T> getInvokers(StimulusEvent<T> event) {
        var eventInvokers = this.listeners.getInvokersOrNull(event);
        var ruleInvokers = this.rules.getInvokersOrNull(event);

        if (eventInvokers != null && ruleInvokers != null) {
            return Iterables.concat(eventInvokers, ruleInvokers);
        } else if (ruleInvokers != null) {
            return ruleInvokers;
        } else if (eventInvokers != null) {
            return eventInvokers;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @NotNull
    public EventResult testRule(GameRuleType rule) {
        return this.rules.test(rule);
    }

    public void onDestroy() {
        this.resources.close();
    }
}
