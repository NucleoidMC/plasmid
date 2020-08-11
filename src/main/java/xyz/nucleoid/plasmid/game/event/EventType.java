package xyz.nucleoid.plasmid.game.event;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.Function;

public final class EventType<T> {
    private final Class<T> type;
    private final Function<T[], T> combine;

    private EventType(Class<T> type, Function<T[], T> combine) {
        this.type = type;
        this.combine = combine;
    }

    public static <T> EventType<T> create(Class<T> type, Function<T[], T> combine) {
        return new EventType<>(type, combine);
    }

    public T combine(T[] listeners) {
        return this.combine.apply(listeners);
    }

    @SuppressWarnings("unchecked")
    public <U> T combineUnchecked(Collection<U> listeners) {
        T[] array = (T[]) Array.newInstance(this.type, listeners.size());
        return this.combine(listeners.toArray(array));
    }

    @SuppressWarnings("unchecked")
    public T createEmpty() {
        T[] array = (T[]) Array.newInstance(this.type, 0);
        return this.combine.apply(array);
    }
}
