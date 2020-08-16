package xyz.nucleoid.plasmid.game.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public final class EventListeners {
    private final Multimap<EventType<?>, Object> listeners = HashMultimap.create();
    private final Reference2ObjectMap<EventType<?>, Object> invokers = new Reference2ObjectOpenHashMap<>();

    public <T> void add(EventType<T> event, T listener) {
        this.listeners.put(event, listener);
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
