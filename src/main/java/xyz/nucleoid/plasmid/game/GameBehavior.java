package xyz.nucleoid.plasmid.game;

import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.stimuli.event.StimulusEvent;

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
     */
    @NotNull
    ActionResult testRule(GameRule rule);
}
