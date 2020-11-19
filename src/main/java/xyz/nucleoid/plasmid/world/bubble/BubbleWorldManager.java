package xyz.nucleoid.plasmid.world.bubble;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.mixin.bubble.MinecraftServerAccess;

import java.io.File;
import java.io.IOException;

public final class BubbleWorldManager {
    private static BubbleWorldManager instance;

    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    private BubbleWorldManager(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;
    }

    public static BubbleWorldManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new BubbleWorldManager(server);
        }
        return instance;
    }

    public BubbleWorld open(BubbleWorldConfig config) {
        Identifier bubbleKey = this.generateBubbleKey();
        RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, bubbleKey);

        DynamicRegistryManager registryManager = this.server.getRegistryManager();

        DimensionType dimensionType = registryManager.getDimensionTypes().get(config.getDimensionType());
        if (dimensionType == null) {
            throw new IllegalStateException(config.getDimensionType() + " dimension type does not exist");
        }

        DimensionOptions dimensionOptions = new DimensionOptions(
                () -> dimensionType,
                config.getGenerator()
        );

        ServerWorld world = this.createWorld(config, worldKey, dimensionOptions);

        BubbleWorld bubble = new BubbleWorld(world, config, bubbleKey);
        ((BubbleWorldHolder) world).setBubbleWorld(bubble);

        this.serverAccess.getWorlds().put(worldKey, world);

        return bubble;
    }

    private ServerWorld createWorld(BubbleWorldConfig config, RegistryKey<World> worldKey, DimensionOptions dimensionOptions) {
        BubbleWorldProperties properties = new BubbleWorldProperties(this.server.getSaveProperties(), config);

        long seed = config.getSeed();
        long biomeSeed = BiomeAccess.hashSeed(seed);

        return new ServerWorld(
                this.server, Util.getMainWorkerExecutor(),
                this.serverAccess.getSession(),
                properties, worldKey, dimensionOptions.getDimensionType(),
                VoidWorldProgressListener.INSTANCE,
                dimensionOptions.getChunkGenerator(),
                false, biomeSeed,
                ImmutableList.of(),
                false
        );
    }

    boolean close(BubbleWorld bubble) {
        RegistryKey<World> dimensionKey = bubble.getDimensionKey();
        ServerWorld world = bubble.getWorld();

        if (this.serverAccess.getWorlds().remove(dimensionKey, world)) {
            LevelStorage.Session session = this.serverAccess.getSession();
            File worldDirectory = session.getWorldDirectory(dimensionKey);
            if (worldDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(worldDirectory);
                } catch (IOException e) {
                    Plasmid.LOGGER.warn("Failed to delete bubble world directory", e);
                    try {
                        FileUtils.forceDeleteOnExit(worldDirectory);
                    } catch (IOException ignored) {
                    }
                }
            }

            return true;
        }

        return false;
    }

    private Identifier generateBubbleKey() {
        String random = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
        return new Identifier(Plasmid.ID, "bubble_" + random);
    }
}
