package net.gegy1000.plasmid.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class GameMapConfig<C extends GameConfig> {
    private final RegistryKey<World> dimension;
    private final BlockPos origin;
    private final MapProvider<C> provider;

    GameMapConfig(RegistryKey<World> dimension, BlockPos origin, MapProvider<C> provider) {
        this.dimension = dimension;
        this.origin = origin;
        this.provider = provider;
    }

    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public MapProvider<C> getProvider() {
        return this.provider;
    }

    public static <C extends GameConfig> Codec<GameMapConfig<C>> codec() {
        return RecordCodecBuilder.create(instance -> {
            Codec<MapProvider<C>> mapProviderCodec = MapProvider.codecUnchecked();
            return instance.group(
                    World.CODEC.fieldOf("dimension").forGetter(GameMapConfig::getDimension),
                    BlockPos.field_25064.fieldOf("origin").forGetter(GameMapConfig::getOrigin),
                    mapProviderCodec.fieldOf("provider").forGetter(GameMapConfig::getProvider)
            ).apply(instance, GameMapConfig::new);
        });
    }
}
