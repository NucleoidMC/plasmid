package xyz.nucleoid.plasmid.api.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

import java.util.function.Consumer;

/**
 * Represents a specific "type" of game. A {@link GameType} is simply responsible for taking a configuration object
 * and setting up game state.
 * <p>
 * A {@link GameType} cannot be directly interacted with from inside the game, but is instead referenced through game
 * configurations which are stored in a datapack.
 *
 * @param <C> the type of config that should be loaded
 * @see GameConfig
 */
public final class GameType<C> {
    private final Identifier id;
    private final MapCodec<C> configCodec;
    private final Open<C> open;

    GameType(Identifier id, MapCodec<C> configCodec, Open<C> open) {
        this.id = id;
        this.configCodec = configCodec;
        this.open = open;
    }

    public GameOpenProcedure open(GameOpenContext<C> context) {
        return this.open.open(context);
    }

    public Identifier id() {
        return this.id;
    }

    public MapCodec<C> configCodec() {
        return this.configCodec;
    }

    public Component name() {
        return Component.translatable(this.translationKey());
    }

    public String translationKey() {
        return Util.makeDescriptionId("gameType", this.id);
    }

    @Nullable
    public static GameType<?> get(Identifier identifier) {
        return PlasmidRegistries.GAME_TYPE.getValue(identifier);
    }

    public interface Open<C> {
        /**
         * Given a game configuration, returns a {@link GameOpenProcedure} describing how this game should be opened.
         * <p>
         * This code runs off-thread, so all blocking or slow operations should run here. Logic interacting with the
         * game should be run in the {@link GameActivity} setup function (see {@link GameOpenContext#open(Consumer)}).
         *
         * @param context the context with which to construct a {@link GameOpenContext} and access configuration
         * @return a {@link GameOpenContext} describing how the game should be opened
         */
        GameOpenProcedure open(GameOpenContext<C> context);
    }
}
