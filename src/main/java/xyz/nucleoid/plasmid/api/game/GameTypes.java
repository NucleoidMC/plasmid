package xyz.nucleoid.plasmid.api.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGame;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGameConfig;

public class GameTypes {
    public static final GameType<RandomGameConfig> RANDOM = register("random", RandomGameConfig.CODEC, RandomGame::open);
    public static final GameType<String> INVALID = register("invalid", MapCodec.unit(""), (context) -> {
        var id = context.server().getRegistryManager().getOrThrow(PlasmidRegistryKeys.GAME_CONFIG).getId(context.game());
        throw new GameOpenException(Text.translatable("text.plasmid.map.open.invalid_game", id != null ? id.toString() : context.game()));
    });

    public static GameType<?> register(Identifier key, GameType<?> type) {
        return Registry.register(PlasmidRegistries.GAME_TYPE, key, type);
    }

    public static <C> GameType<C> register(Identifier key, MapCodec<C> configCodec, GameType.Open<C> open) {
        var type = new GameType<>(key, configCodec, open);
        register(key, type);
        return type;
    }

    public static <C> GameType<C> register(String key, MapCodec<C> configCodec, GameType.Open<C> open) {
        return register(Plasmid.id(key), configCodec, open);
    }
}
