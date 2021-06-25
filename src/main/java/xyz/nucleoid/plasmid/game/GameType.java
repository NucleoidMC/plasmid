package xyz.nucleoid.plasmid.game;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

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
    public static final TinyRegistry<GameType<?>> REGISTRY = TinyRegistry.create();

    private final Identifier identifier;
    private final Codec<C> configCodec;
    private final Open<C> open;

    private GameType(Identifier identifier, Codec<C> configCodec, Open<C> open) {
        this.identifier = identifier;
        this.configCodec = configCodec;
        this.open = open;
    }

    /**
     * Registers a new {@link GameType} with the given id, codec to parse a config, and function to set up the game.
     *
     * @param identifier a unique identifier to register this game type with
     * @param configCodec a {@link Codec} that can deserialize
     * @param open a function that describes how the game should be set up, given a configuration
     * @param <C> the type of config that should be loadedS
     * @return the registered {@link GameType} instance
     * @see Codec
     * @see com.mojang.serialization.codecs.RecordCodecBuilder
     */
    public static <C> GameType<C> register(Identifier identifier, Codec<C> configCodec, Open<C> open) {
        GameType<C> type = new GameType<>(identifier, configCodec, open);
        REGISTRY.register(identifier, type);
        return type;
    }

    public GameOpenProcedure open(GameOpenContext<C> context) {
        return this.open.open(context);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Codec<C> getConfigCodec() {
        return this.configCodec;
    }

    public Text getName() {
        return new TranslatableText(this.getTranslationKey());
    }

    public String getTranslationKey() {
        return "game." + this.identifier.getNamespace() + "." + this.identifier.getPath();
    }

    @Nullable
    public static GameType<?> get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    public interface Open<C> {
        GameOpenProcedure open(GameOpenContext<C> context);
    }
}
