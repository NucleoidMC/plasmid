package net.gegy1000.plasmid.game.map.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RandomMapProvider<C extends GameConfig> implements MapProvider<C> {
    private final List<MapProvider<C>> providers;

    public RandomMapProvider(List<MapProvider<C>> providers) {
        this.providers = providers;
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, C config) {
        int index = world.random.nextInt(this.providers.size());
        MapProvider<C> provider = this.providers.get(index);
        return provider.createAt(world, origin, config);
    }

    @Override
    public Codec<? extends MapProvider<?>> getCodec() {
        return codec();
    }

    public static <C extends GameConfig> Codec<RandomMapProvider<C>> codec() {
        return RecordCodecBuilder.create(instance -> {
            Codec<MapProvider<C>> providerCodec = MapProvider.codecUnchecked();
            return instance.group(
                    providerCodec.listOf().fieldOf("providers").forGetter(rand -> rand.providers)
            ).apply(instance, RandomMapProvider::new);
        });
    }
}
