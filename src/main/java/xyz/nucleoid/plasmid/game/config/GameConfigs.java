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
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.IOException;
import java.util.Set;

public final class GameConfigs {
    private static final TinyRegistry<GameConfig<?>> CONFIGS = TinyRegistry.create();
    public static DynamicRegistryManager registryManager;

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Plasmid.ID, "games");
            }

            @Override
            public void reload(ResourceManager manager) {
                CONFIGS.clear();

                DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryManager);
                registryManager = null;

                manager.findResources("games", path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
                    try {
                        try (var reader = resource.getReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            Identifier identifier = identifierFromPath(path);

                            Codec<GameConfig<?>> codec = GameConfig.codecFrom(identifier);
                            DataResult<GameConfig<?>> result = codec.parse(ops, json);

                            result.result().ifPresent(game -> {
                                CONFIGS.register(identifier, game);
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
            }
        });
    }

    private static Identifier identifierFromPath(Identifier location) {
        String path = location.getPath();
        path = path.substring("games/".length(), path.length() - ".json".length());
        return new Identifier(location.getNamespace(), path);
    }

    @Nullable
    public static GameConfig<?> get(Identifier identifier) {
        return CONFIGS.get(identifier);
    }

    public static Set<Identifier> getKeys() {
        return CONFIGS.keySet();
    }
}
