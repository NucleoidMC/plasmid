package xyz.nucleoid.plasmid.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class GameConfigs implements GameConfigList {
    private static final ResourceFinder FINDER = ResourceFinder.json("games");

    private static GameConfigs instance = new GameConfigs(Map.of());

    private final Map<Identifier, GameConfig<?>> configs;
    public static DynamicRegistryManager registryManager;

    private GameConfigs(Map<Identifier, GameConfig<?>> configs) {
        this.configs = configs;
    }

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Plasmid.ID, "games");
            }

            @Override
            public void reload(ResourceManager manager) {
                DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryManager);
                registryManager = null;

                Map<Identifier, GameConfig<?>> configs = new HashMap<>();
                FINDER.findResources(manager).forEach((path, resource) -> {
                    try {
                        try (var reader = resource.getReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            Identifier identifier = FINDER.toResourceId(path);

                            Codec<GameConfig<?>> codec = GameConfig.codecFrom(identifier);
                            DataResult<GameConfig<?>> result = codec.parse(ops, json);

                            result.result().ifPresent(game -> {
                                configs.put(identifier, game);
                            });

                            result.error().ifPresent(error -> {
                                Plasmid.LOGGER.error("Failed to parse game at {}: {}", path, error.toString());
                            });
                        }
                    } catch (IOException e) {
                        Plasmid.LOGGER.error("Failed to read configured game at {}", path, e);
                    } catch (JsonParseException e) {
                        Plasmid.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
                    }
                });

                initialize(configs);
            }
        });
    }

    private static void initialize(Map<Identifier, GameConfig<?>> configs) {
        if (instance != null) {
            GameConfigLists.unregister(instance);
        }
        instance = new GameConfigs(Map.copyOf(configs));
        GameConfigLists.register(instance);
    }

    public static GameConfigs get() {
        return instance;
    }

    /**
     * @deprecated use {@link GameConfigs#get()} and {@link GameConfigs#byKey(Identifier)}
     */
    @Nullable
    @Deprecated
    public static GameConfig<?> get(Identifier identifier) {
        return get().byKey(identifier);
    }

    /**
     * @deprecated use {@link GameConfigs#get()} and {@link GameConfigs#keys()}
     */
    public static Set<Identifier> getKeys() {
        return get().configs.keySet();
    }

    @Override
    @Nullable
    public GameConfig<?> byKey(Identifier key) {
        return this.configs.get(key);
    }

    @Override
    public Stream<Identifier> keys() {
        return this.configs.keySet().stream();
    }
}
