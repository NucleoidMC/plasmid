package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This object is passed to game constructors, holding all relevant information needed to construct the game as well as
 * providing the access through which to create a {@link GameSpace}.
 *
 * @param <C> the config type passed for this game
 * @see GameOpenProcedure
 * @see GameType.Open
 */
public record GameOpenContext<C>(MinecraftServer server, GameConfig<C> game) {
    /**
     * Creates a {@link GameOpenProcedure} that opens a game given the {@code setup} function.
     * <p>
     * This setup function should set any rules or event listeners on the given {@link GameActivity} needed for it to
     * function. The setup function furthermore runs on-thread and should not run any slow operations.
     *
     * @param setup the setup function for the newly constructed {@link GameActivity}
     * @return a {@link GameOpenProcedure} which should be returned by a game constructor
     * @see GameActivity
     * @see GameActivity#listen(StimulusEvent, Object)
     * @see GameActivity#setRule(GameRuleType, ActionResult)
     */
    public GameOpenProcedure open(Consumer<GameActivity> setup) {
        return gameSpace -> gameSpace.setActivity(setup);
    }

    /**
     * Creates a {@link GameOpenProcedure} that opens a game given the {@code setup} function and creates a world.
     * <p>
     * This setup function should set any rules or event listeners on the given {@link GameActivity} needed for it to
     * function. The setup function furthermore runs on-thread and should not run any slow operations.
     *
     * @param setup the setup function for the newly constructed {@link GameActivity}
     * @param worldConfig the configuration describing how the added world should be constructed
     * @return a {@link GameOpenProcedure} which should be returned by a game constructor
     * @see GameActivity
     * @see GameActivity#listen(StimulusEvent, Object)
     * @see GameActivity#setRule(GameRuleType, ActionResult)
     */
    public GameOpenProcedure openWithWorld(RuntimeWorldConfig worldConfig, BiConsumer<GameActivity, ServerWorld> setup) {
        return this.open(activity -> {
            ServerWorld world = activity.getGameSpace().addWorld(worldConfig);
            setup.accept(activity, world);
        });
    }

    /**
     * @return the configuration that this game was opened with
     */
    public C config() {
        return this.game.config();
    }
}
