package xyz.nucleoid.plasmid.game.rule;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.EventListenerMap;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public final class GameRuleMap {
    private final Reference2ObjectMap<GameRuleType, ActionResult> rules = new Reference2ObjectOpenHashMap<>();
    private EventListenerMap listeners = new EventListenerMap();

    public static GameRuleMap empty() {
        return new GameRuleMap();
    }

    public void set(GameRuleType rule, ActionResult result) {
        if (this.trySet(rule, result)) {
            this.listeners = this.buildListeners();
        }
    }

    @NotNull
    public ActionResult test(GameRuleType rule) {
        return this.rules.getOrDefault(rule, ActionResult.PASS);
    }

    @Nullable
    public <T> Iterable<T> getInvokersOrNull(StimulusEvent<T> event) {
        var listeners = this.listeners.get(event);
        return !listeners.isEmpty() ? listeners : null;
    }

    private boolean trySet(GameRuleType rule, ActionResult result) {
        if (result != ActionResult.PASS) {
            return this.rules.put(rule, result) != result;
        } else {
            return this.rules.remove(rule) != null;
        }
    }

    private EventListenerMap buildListeners() {
        var listeners = new EventListenerMap();

        for (var entry : this.rules.entrySet()) {
            var rule = entry.getKey();
            var result = entry.getValue();

            rule.enforce(listeners, result);
        }

        return listeners;
    }
}
