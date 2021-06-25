package xyz.nucleoid.plasmid.game.rule;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.EventListenerMap;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collection;
import java.util.Map;

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
        Collection<T> listeners = this.listeners.get(event);
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
        EventListenerMap listeners = new EventListenerMap();

        for (Map.Entry<GameRuleType, ActionResult> entry : this.rules.entrySet()) {
            GameRuleType rule = entry.getKey();
            ActionResult result = entry.getValue();

            rule.enforce(listeners, result);
        }

        return listeners;
    }
}
