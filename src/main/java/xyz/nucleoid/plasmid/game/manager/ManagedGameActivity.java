package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.Iterables;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.error.ErrorReporter;
import xyz.nucleoid.plasmid.game.GameResources;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.activity.GameActivity;
import xyz.nucleoid.plasmid.game.activity.GameActivitySource;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameEventExceptionHandler;
import xyz.nucleoid.plasmid.game.event.GameEventListeners;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.rule.GameRuleMap;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collections;

public final class ManagedGameActivity implements GameActivity {
    private final ManagedGameSpace space;
    private final GameConfig<?> config;
    private final GameActivitySource source;

    private final ErrorReporter errorReporter;

    private final GameEventListeners listeners;
    private final GameRuleMap rules = new GameRuleMap();
    private final GameResources resources = new GameResources();

    ManagedGameActivity(ManagedGameSpace space, GameConfig<?> config, GameActivitySource source) {
        this.space = space;
        this.config = config;
        this.source = source;

        this.errorReporter = ErrorReporter.open(config);

        GameEventExceptionHandler exceptionHandler = createExceptionHandler(this.errorReporter);
        this.listeners = new GameEventListeners(exceptionHandler);
    }

    private static GameEventExceptionHandler createExceptionHandler(ErrorReporter errorReporter) {
        return new GameEventExceptionHandler() {
            @Override
            public <T> void handleException(StimulusEvent<T> event, Throwable throwable) {
                String listenerName = event.getListenerType().getSimpleName();

                Plasmid.LOGGER.warn("An unexpected exception occurred while invoking {}", listenerName, throwable);
                errorReporter.report(throwable, "Invoking " + listenerName);
            }
        };
    }

    @Override
    public GameSpace getGameSpace() {
        return this.space;
    }

    @Override
    public GameConfig<?> getGameConfig() {
        return this.config;
    }

    @Override
    public GameActivitySource getSource() {
        return this.source;
    }

    @Override
    public ManagedGameActivity setRule(GameRuleType rule, ActionResult result) {
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
        Iterable<T> eventInvokers = this.listeners.getInvokersOrNull(event);
        Iterable<T> ruleInvokers = this.rules.getInvokersOrNull(event);

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
    public ActionResult testRule(GameRuleType rule) {
        return this.rules.test(rule);
    }

    public void onDestroy() {
        this.resources.close();
        this.errorReporter.close();
    }
}
