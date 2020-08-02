package net.gegy1000.plasmid.game.map.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.map.GameMapData;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class PathMapProvider<C extends GameConfig> implements MapProvider<C> {
    public static final Codec<PathMapProvider<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(PathMapProvider::getPath)
    ).apply(instance, PathMapProvider::new));

    private final Identifier path;

    public PathMapProvider(Identifier path) {
        this.path = path;
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, C config) {
        MinecraftServer server = world.getServer();
        return GameMapData.load(this.path).thenApplyAsync(data -> {
            BlockBounds bounds = data.getBounds();
            BlockPos size = bounds.getSize();

            return data.addToWorld(world, origin.add(-size.getX() / 2, 0, -size.getZ() / 2));
        }, server);
    }

    public Identifier getPath() {
        return this.path;
    }

    @Override
    public Codec<? extends MapProvider<?>> getCodec() {
        return CODEC;
    }
}
