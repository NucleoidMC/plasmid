package xyz.nucleoid.plasmid.game.event;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.EventInvokerContext;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public final class GameEventListeners implements EventRegistrar {
    private final GameEventExceptionHandler exceptionHandler;

    private final Reference2ObjectMap<StimulusEvent<?>, List<Object>> listeners = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<StimulusEvent<?>, InvokerEntry<?>> invokers = new Reference2ObjectOpenHashMap<>();

    public GameEventListeners(GameEventExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public <T> void listen(StimulusEvent<T> event, T listener) {
        this.listeners.computeIfAbsent(event, e -> new ArrayList<>()).add(listener);
        this.updateInvoker(event);
    }

    @Override
    public <T> void unlisten(StimulusEvent<T> event, T listener) {
        var listeners = this.listeners.get(event);
        if (listeners != null && listeners.remove(listener)) {
            if (listeners.isEmpty()) {
                this.listeners.remove(event);
            }
            this.updateInvoker(event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void updateInvoker(StimulusEvent<T> event) {
        var listeners = (List<T>) this.listeners.get(event);
        if (listeners != null && !listeners.isEmpty()) {
            var entry = (InvokerEntry<T>) this.invokers.get(event);
            if (entry != null) {
                entry.listeners = listeners;
            } else {
                this.invokers.put(event, new InvokerEntry<>(event, listeners));
            }
        } else {
            this.invokers.remove(event);
        }
    }

    @NotNull
    public <T> T getInvoker(StimulusEvent<T> event) {
        var entry = this.getInvokerEntry(event);
        return entry != null ? entry.invoker : event.emptyInvoker();
    }

    @NotNull
    public <T> T getPropagatingInvoker(StimulusEvent<T> event) {
        var entry = this.getInvokerEntry(event);
        return entry != null ? entry.propagatingInvoker : event.emptyInvoker();
    }

    @NotNull
    public <T> Iterable<T> getInvokers(StimulusEvent<T> event) {
        var entry = this.getInvokerEntry(event);
        return entry != null ? entry : Collections.emptyList();
    }

    @Nullable
    public <T> Iterable<T> getInvokersOrNull(StimulusEvent<T> event) {
        return this.getInvokerEntry(event);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> InvokerEntry<T> getInvokerEntry(StimulusEvent<T> event) {
        return (InvokerEntry<T>) this.invokers.get(event);
    }

    final class InvokerEntry<T> implements Iterable<T> {
        private final StimulusEvent<T> event;
        private List<T> listeners;

        private final T invoker;
        private final T propagatingInvoker;

        InvokerEntry(StimulusEvent<T> event, List<T> listeners) {
            this.event = event;
            this.listeners = listeners;

            var exceptionHandler = GameEventListeners.this.exceptionHandler;

            this.invoker = this.createInvoker(throwable -> {
                exceptionHandler.handleException(event, throwable);
            });

            this.propagatingInvoker = this.createInvoker(throwable -> {
                exceptionHandler.handleException(event, throwable);

                Throwables.throwIfUnchecked(throwable);
                throw new RuntimeException(throwable);
            });
        }

        private T createInvoker(Consumer<Throwable> exceptionHandler) {
            return this.event.createInvoker(new EventInvokerContext<T>() {
                @Override
                public Iterable<T> getListeners() {
                    return InvokerEntry.this.listeners;
                }

                @Override
                public void handleException(Throwable throwable) {
                    exceptionHandler.accept(throwable);
                }
            });
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return Iterators.singletonIterator(this.invoker);
        }
    }
}
