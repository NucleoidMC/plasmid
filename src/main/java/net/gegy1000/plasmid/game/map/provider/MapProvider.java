package net.gegy1000.plasmid.game.map.provider;

import com.mojang.serialization.Codec;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.registry.TinyRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface MapProvider<C extends GameConfig> {
    TinyRegistry<Codec<? extends MapProvider<?>>> REGISTRY = TinyRegistry.newStable();

    Codec<MapProvider<?>> CODEC = REGISTRY.dispatchStable(MapProvider::getCodec, Function.identity());

    CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, C config);

    Codec<? extends MapProvider<?>> getCodec();

    @SuppressWarnings("unchecked")
    static <C extends GameConfig> Codec<MapProvider<C>> codecUnchecked() {
        return (Codec<MapProvider<C>>) (Codec<?>) CODEC;
    }
}
