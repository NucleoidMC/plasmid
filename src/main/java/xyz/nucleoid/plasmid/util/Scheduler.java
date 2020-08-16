package xyz.nucleoid.plasmid.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;

public final class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private final LinkedList<Task> taskQueue = new LinkedList<>();
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
     *
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
        this.taskQueue.add(new OneshotTask(task, this.currentTick + delay));
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
        int beginTime = this.currentTick + delay;
        this.enqueue(new DoWhileTask(task, condition, beginTime, interval));
    }

    private void enqueue(Task task) {
        this.taskQueue.add(task);
    }

    private void runTasks(MinecraftServer server) {
        int time = server.getTicks();
        this.currentTick = time;

        Iterator<Task> iterator = this.taskQueue.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.tryRun(server, time)) {
                iterator.remove();
            }
        }
    }

    private interface Task {
        boolean tryRun(MinecraftServer server, int time);
    }

    private static class OneshotTask implements Task {
        private final Consumer<MinecraftServer> action;
        private final int time;

        OneshotTask(Consumer<MinecraftServer> action, int time) {
            this.action = action;
            this.time = time;
        }

        @Override
        public boolean tryRun(MinecraftServer server, int time) {
            if (time >= this.time) {
                this.action.accept(server);
                return true;
            }
            return false;
        }
    }

    private static class DoWhileTask implements Task {
        private final Consumer<MinecraftServer> task;
        private final IntPredicate condition;
        private final int interval;

        private int nextTime;

        private DoWhileTask(Consumer<MinecraftServer> task, IntPredicate condition, int beginTime, int interval) {
            this.task = task;
            this.condition = condition;
            this.nextTime = beginTime;
            this.interval = interval;
        }

        @Override
        public boolean tryRun(MinecraftServer server, int time) {
            if (time >= this.nextTime) {
                this.task.accept(server);
                this.nextTime = time + this.interval;

                return !this.shouldRepeat(time);
            }

            return false;
        }

        private boolean shouldRepeat(int predicate) {
            IntPredicate condition = this.condition;
            return condition == null || condition.test(predicate);
        }
    }
}
