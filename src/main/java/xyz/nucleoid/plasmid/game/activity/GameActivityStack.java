package xyz.nucleoid.plasmid.game.activity;

import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameBehavior;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

public final class GameActivityStack<A extends GameActivity> implements GameBehavior {
    private final Deque<A> stack = new ArrayDeque<>();

    public void push(A activity) {
        this.stack.addLast(activity);
    }

    @Nullable
    public A pop() {
        return this.stack.pollLast();
    }

    @Nullable
    public A peek() {
        return this.stack.peekLast();
    }

    public boolean isEnabled(A activity) {
        return this.peek() == activity;
    }

    public boolean pop(A activity) {
        if (this.isEnabled(activity)) {
            this.pop();
            return true;
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    @NotNull
    public <T> T invoker(StimulusEvent<T> event) {
        A activity = this.stack.peekLast();
        if (activity != null) {
            return activity.invoker(event);
        } else {
            return event.emptyInvoker();
        }
    }

    @Override
    @NotNull
    public <T> T propagatingInvoker(StimulusEvent<T> event) {
        A activity = this.stack.peekLast();
        if (activity != null) {
            return activity.propagatingInvoker(event);
        } else {
            return event.emptyInvoker();
        }
    }

    @Override
    @NotNull
    public <T> Iterable<T> getInvokers(StimulusEvent<T> event) {
        A activity = this.stack.peekLast();
        if (activity != null) {
            return activity.getInvokers(event);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @NotNull
    public ActionResult testRule(GameRuleType rule) {
        A activity = this.stack.peekLast();
        if (activity != null) {
            return activity.testRule(rule);
        } else {
            return ActionResult.PASS;
        }
    }
}
