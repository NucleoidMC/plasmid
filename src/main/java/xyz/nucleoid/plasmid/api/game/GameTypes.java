package xyz.nucleoid.plasmid.api.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGame;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGameConfig;

public final class GameTypes {
    private GameTypes() {}

    public static final GameType<RandomGameConfig> RANDOM = register("random", RandomGameConfig.CODEC, RandomGame::open);
    public static final GameType<String> INVALID = register("invalid", MapCodec.unit(""), (context) -> {
        var id = context.server().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_CONFIG).getKey(context.game());
        throw new GameOpenException(Component.translatable("text.plasmid.map.open.invalid_game", id != null ? id.toString() : context.game()));
    });

    public static GameType<?> register(Identifier key, GameType<?> type) {
        return Registry.register(PlasmidRegistries.GAME_TYPE, key, type);
    }


    /**
     * Registers a new {@link GameType} with the given id, codec to parse a config, and function to set up the game.
     *
     * @param key a unique identifier to register this game type with
     * @param configCodec a {@link MapCodec} that can deserialize
     * @param open a function that describes how the game should be set up, given a configuration
     * @param <C> the type of config that should be loadedS
     * @return the registered {@link GameType} instance
     * @see MapCodec
     * @see com.mojang.serialization.codecs.RecordCodecBuilder
     */
    public static <C> GameType<C> register(Identifier key, MapCodec<C> configCodec, GameType.Open<C> open) {
        var type = new GameType<>(key, configCodec, open);
        register(key, type);
        return type;
    }

    public static <C> GameType<C> register(String key, MapCodec<C> configCodec, GameType.Open<C> open) {
        return register(Plasmid.id(key), configCodec, open);
    }
}
