package xyz.nucleoid.plasmid.game;

import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collections;

/**
 * Represents the logic that describes how a {@link GameSpace} or {@link GameActivity} interacts with the world.
 *
 * This contains both event listeners, allowing games to respond to specific events within the world, as well as
 * containing rules which can simply allow or disallow certain behavior in the world.
 *
 * @see StimulusEvent
 * @see GameRuleType
 * @see GameSpace
 * @see GameActivity
 */
public interface GameBehavior {
    GameBehavior EMPTY = new GameBehavior() {
        @Override
        @NotNull
        public <T> T invoker(StimulusEvent<T> event) {
            return event.emptyInvoker();
        }

        @Override
        @NotNull
        public <T> T propagatingInvoker(StimulusEvent<T> event) {
            return event.emptyInvoker();
        }

        @Override
        @NotNull
        public <T> Iterable<T> getInvokers(StimulusEvent<T> event) {
            return Collections.emptyList();
        }

        @Override
        @NotNull
        public EventResult testRule(GameRuleType rule) {
            return EventResult.DENY;
        }
    };

    @NotNull
    <T> T invoker(StimulusEvent<T> event);

    @NotNull
    <T> T propagatingInvoker(StimulusEvent<T> event);

    @NotNull
    <T> Iterable<T> getInvokers(StimulusEvent<T> event);

    /**
     * Tests whether the given {@link GameRuleType} passes in this {@link GameBehavior}.
     *
     * <p>As an example, calling this method with {@link GameRuleType#BLOCK_DROPS} will return a {@link EventResult} that describes whether blocks can drop.
     *
     * @param rule the {@link GameRuleType} to test in this {@link GameBehavior}
     * @return a {@link EventResult} that describes whether the {@link GameRuleType} passes
     *
     * @see EventResult
     */
    @NotNull
    EventResult testRule(GameRuleType rule);
}
