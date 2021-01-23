package xyz.nucleoid.plasmid.game.event;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class EventListeners {
    private final Reference2ObjectMap<EventType<?>, List<Object>> listeners = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<EventType<?>, Object> invokers = new Reference2ObjectOpenHashMap<>();

    public <T> void add(EventType<T> event, T listener) {
        List<Object> listeners = this.listeners.computeIfAbsent(event, e -> new ArrayList<>());
        listeners.add(listener);
        this.rebuildInvoker(event);
    }

    private <T> void rebuildInvoker(EventType<T> event) {
        Object combined = event.combineUnchecked(this.listeners.get(event));
        this.invokers.put(event, combined);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T invoker(EventType<T> event) {
        return (T) this.invokers.computeIfAbsent(event, EventType::createEmpty);
    }
}
