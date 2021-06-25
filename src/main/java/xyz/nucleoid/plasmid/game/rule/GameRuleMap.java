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
    private final Reference2ObjectMap<GameRule, ActionResult> rules = new Reference2ObjectOpenHashMap<>();
    private EventListenerMap listeners = new EventListenerMap();

    public static GameRuleMap empty() {
        return new GameRuleMap();
    }

    public void set(GameRule rule, ActionResult result) {
        if (this.trySet(rule, result)) {
            this.listeners = this.buildListeners();
        }
    }

    @NotNull
    public ActionResult test(GameRule rule) {
        return this.rules.getOrDefault(rule, ActionResult.PASS);
    }

    @Nullable
    public <T> Iterable<T> getInvokersOrNull(StimulusEvent<T> event) {
        Collection<T> listeners = this.listeners.get(event);
        return !listeners.isEmpty() ? listeners : null;
    }

    private boolean trySet(GameRule rule, ActionResult result) {
        if (result != ActionResult.PASS) {
            return this.rules.put(rule, result) != result;
        } else {
            return this.rules.remove(rule) != null;
        }
    }

    private EventListenerMap buildListeners() {
        EventListenerMap listeners = new EventListenerMap();

        for (Map.Entry<GameRule, ActionResult> entry : this.rules.entrySet()) {
            GameRule rule = entry.getKey();
            ActionResult result = entry.getValue();

            rule.enforce(listeners, result);
        }

        return listeners;
    }
}
