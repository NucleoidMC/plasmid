package net.gegy1000.plasmid.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.game.ConfiguredGame;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class GameConfigs {
    private static final Map<Identifier, ConfiguredGame<?>> CONFIGURED_GAMES = new HashMap<>();

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Plasmid.ID, "games");
            }

            @Override
            public void apply(ResourceManager manager) {
                CONFIGURED_GAMES.clear();

                Identifier root = new Identifier(Plasmid.ID, "games");
                Collection<Identifier> resources = manager.findResources(root, path -> path.endsWith(".json"));

                for (Identifier path : resources) {
                    try {
                        Resource resource = manager.getResource(path);
                        try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                            JsonElement json = new JsonParser().parse(reader);

                            Identifier identifier = identifierFromPath(path);

                            DataResult<Pair<ConfiguredGame<?>, JsonElement>> decode = ConfiguredGame.CODEC.decode(JsonOps.INSTANCE, json);

                            decode.result().ifPresent(result -> {
                                ConfiguredGame<?> game = result.getFirst();
                                CONFIGURED_GAMES.put(identifier, game);
                            });

                            decode.error().ifPresent(error -> {
                                Plasmid.LOGGER.error("Failed to decode {}: {}", path, error.toString());
                            });
                        }
                    } catch (IOException e) {
                        Plasmid.LOGGER.error("Failed to read configured game at {}", path, e);
                    }
                }
            }
        });
    }

    private static Identifier identifierFromPath(Identifier location) {
        String path = location.getPath();
        path = path.substring("games/".length(), path.length() - ".json".length());

        int idx = path.indexOf('/');
        if (idx == -1) {
            return new Identifier(location.getNamespace(), path);
        }

        return new Identifier(path.substring(0, idx), path.substring(idx + 1));
    }

    @Nullable
    public static ConfiguredGame<?> get(Identifier identifier) {
        return CONFIGURED_GAMES.get(identifier);
    }

    public static Set<Identifier> getKeys() {
        return CONFIGURED_GAMES.keySet();
    }
}
