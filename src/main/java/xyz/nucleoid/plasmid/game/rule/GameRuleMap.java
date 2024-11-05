package xyz.nucleoid.plasmid.game.rule;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.EventListenerMap;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Map;

public final class GameRuleMap {
    private final Reference2ObjectMap<GameRuleType, EventResult> rules = new Reference2ObjectOpenHashMap<>();
    private EventListenerMap listeners = null;

    public static GameRuleMap empty() {
        return new GameRuleMap();
    }

    public void set(GameRuleType rule, EventResult result) {
        if (this.trySet(rule, result)) {
            this.listeners = null;
        }
    }

    @NotNull
    public EventResult test(GameRuleType rule) {
        return this.rules.getOrDefault(rule, EventResult.PASS);
    }

    @Nullable
    public <T> Iterable<T> getInvokersOrNull(StimulusEvent<T> event) {
        var listeners = this.getListeners().get(event);
        return !listeners.isEmpty() ? listeners : null;
    }

    private boolean trySet(GameRuleType rule, EventResult result) {
        if (result != EventResult.PASS) {
            return this.rules.put(rule, result) != result;
        } else {
            return this.rules.remove(rule) != null;
        }
    }

    private EventListenerMap getListeners() {
        var listeners = this.listeners;
        if (listeners == null) {
            this.listeners = listeners = this.buildListeners();
        }
        return listeners;
    }

    private EventListenerMap buildListeners() {
        var listeners = new EventListenerMap();

        this.rules.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(GameRuleType.COMPARATOR))
                .forEach(entry -> {
                    var rule = entry.getKey();
                    var result = entry.getValue();

                    rule.enforce(listeners, result);
                });

        return listeners;
    }
}
