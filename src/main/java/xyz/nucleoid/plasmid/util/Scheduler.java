package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;

public final class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private final Int2ObjectMap<List<Consumer<MinecraftServer>>> taskQueue = new Int2ObjectOpenHashMap<>();
    private int currentTick = 0;

    private Scheduler() {
        ServerTickEvents.END_SERVER_TICK.register(this::runTasks);
    }

    /**
     * queue a one-shot task to be executed next tick on the server thread and capture the result in a future
     *
     * @param task the action to perform
     */
    public <T> CompletableFuture<T> submit(Function<MinecraftServer, T> task) {
        return this.submit(task, 1);
    }

    /**
     * queue a one time task to be executed on the server thread and capture the result in a future
     *
     * @param delay how many ticks in the future this should be called, where 0 means at the end of the current tick
     * @param task the action to perform
     */
    public <T> CompletableFuture<T> submit(Function<MinecraftServer, T> task, int delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.submit(server -> {
            T result = task.apply(server);
            future.complete(result);
        }, delay);
        return future;
    }

    /**
     * queue a one-shot task to be executed next tick on the server thread
     * @param task the action to perform
     */
    public void submit(Consumer<MinecraftServer> task) {
        this.submit(task, 1);
    }

    /**
     * queue a one time task to be executed on the server thread
     *
     * @param delay how many ticks in the future this should be called, where 0 means at the end of the current tick
     * @param task the action to perform
     */
    public void submit(Consumer<MinecraftServer> task, int delay) {
        int timestamp = this.currentTick + delay + 1;
        this.taskQueue.computeIfAbsent(timestamp, t -> new ArrayList<>()).add(task);
    }

    /**
     * schedule a repeating task that is executed infinitely every n ticks
     *
     * @param task the action to perform
     * @param delay how many ticks in the future this event should first be called
     * @param interval the number of ticks in between each execution
     */
    public void repeat(Consumer<MinecraftServer> task, int delay, int interval) {
        this.repeatWhile(task, null, delay, interval);
    }

    /**
     * repeat the given task until the predicate returns false
     *
     * @param task the action to perform
     * @param condition whether or not to reschedule the task again, with the parameter being the current tick
     * @param delay how many ticks in the future this event should first be called
     * @param interval the number of ticks in between each execution
     */
    public void repeatWhile(Consumer<MinecraftServer> task, IntPredicate condition, int delay, int interval) {
        this.submit(new Repeating(task, condition, interval), delay);
    }

    /**
     * repeat the given task n times more than 1 time
     *
     * @param task the action to perform
     * @param times the number of times the task should be scheduled
     * @param delay how many ticks in the future this event should first be called
     * @param interval the number of ticks in between each execution
     */
    public void repeatN(Consumer<MinecraftServer> task, int times, int delay, int interval) {
        this.repeatWhile(task, new IntPredicate() {
            private int remaining = times;

            @Override
            public boolean test(int value) {
                return --this.remaining > 0;
            }
        }, delay, interval);
    }

    private void runTasks(MinecraftServer server) {
        this.currentTick = server.getTicks();

        List<Consumer<MinecraftServer>> tasks = this.taskQueue.remove(this.currentTick);
        if (tasks == null) {
            return;
        }

        for (Consumer<MinecraftServer> task : tasks) {
            task.accept(server);
        }
    }

    private final class Repeating implements Consumer<MinecraftServer> {
        private final Consumer<MinecraftServer> task;
        private final IntPredicate repeatCondition;
        public final int interval;

        private Repeating(Consumer<MinecraftServer> task, IntPredicate repeatCondition, int interval) {
            this.task = task;
            this.repeatCondition = repeatCondition;
            this.interval = interval;
        }

        @Override
        public void accept(MinecraftServer server) {
            this.task.accept(server);

            if (this.shouldRepeat(Scheduler.this.currentTick)) {
                Scheduler.this.submit(this, this.interval);
            }
        }

        private boolean shouldRepeat(int predicate) {
            if (this.repeatCondition == null) {
                return true;
            }
            return this.repeatCondition.test(predicate);
        }
    }
}
