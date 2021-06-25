package xyz.nucleoid.plasmid.game;

import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.activity.GameActivity;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Represents the logic that describes how a {@link GameSpace} or {@link GameActivity} interacts with the world.
 *
 * This contains both event listeners, allowing games to respond to specific events within the world, as well as
 * containing rules which can simply allow or disallow certain behavior in the world.
 *
 * @see StimulusEvent
 * @see GameRule
 * @see GameSpace
 * @see GameActivity
 */
public interface GameBehavior {
    @NotNull
    <T> T invoker(StimulusEvent<T> event);

    @NotNull
    <T> T propagatingInvoker(StimulusEvent<T> event);

    @NotNull
    <T> Iterable<T> getInvokers(StimulusEvent<T> event);

    /**
     * Tests whether the given {@link GameRule} passes in this {@link GameBehavior}.
     *
     * <p>As an example, calling this method with {@link GameRule#BLOCK_DROPS} will return a {@link ActionResult} that describes whether blocks can drop.
     *
     * @param rule the {@link GameRule} to test in this {@link GameBehavior}
     * @return a {@link ActionResult} that describes whether the {@link GameRule} passes
     *
     * @see ActionResult
     */
    @NotNull
    ActionResult testRule(GameRule rule);
}
