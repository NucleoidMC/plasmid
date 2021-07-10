package xyz.nucleoid.plasmid.game.portal;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class GamePortalManager {
    public static final GamePortalManager INSTANCE = new GamePortalManager();

    private static final long DISPLAY_UPDATE_INTERVAL = 20L;

    private static final String PATH = "game_portals";

    private final TinyRegistry<GamePortal> portals = TinyRegistry.create();
    private Map<Identifier, GamePortalConfig> portalQueue;

    private MinecraftServer server;
    private long lastDisplayUpdateTime;

    private GamePortalManager() {
    }

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Plasmid.ID, "game_portals");
            }

            @Override
            public void reload(ResourceManager manager) {
                INSTANCE.reload(manager);
            }
        });
    }

    public void setup(MinecraftServer server) {
        this.server = server;

        Map<Identifier, GamePortalConfig> queue = this.portalQueue;
        if (queue != null) {
            this.portalQueue = null;
            this.loadPortalsFrom(server, queue);
        }
    }

    public void close(MinecraftServer server) {
        if (this.server == server) {
            this.server = null;
            this.portals.clear();
            this.portalQueue = null;
        }
    }

    private void reload(ResourceManager manager) {
        this.portals.clear();

        Map<Identifier, GamePortalConfig> configs = this.loadConfigs(manager);
        if (this.server != null) {
            this.loadPortalsFrom(this.server, configs);
        } else {
            this.portalQueue = configs;
        }
    }

    private void loadPortalsFrom(MinecraftServer server, Map<Identifier, GamePortalConfig> queue) {
        for (Map.Entry<Identifier, GamePortalConfig> entry : queue.entrySet()) {
            Identifier identifier = entry.getKey();
            GamePortalConfig config = entry.getValue();

            GamePortal portal = new GamePortal(server, identifier, config::createBackend);
            portal.setCustom(config.getCustom());

            this.portals.register(identifier, portal);
        }
    }

    private Map<Identifier, GamePortalConfig> loadConfigs(ResourceManager manager) {
        Map<Identifier, GamePortalConfig> configs = new Object2ObjectOpenHashMap<>();

        Collection<Identifier> resources = manager.findResources(PATH, path -> path.endsWith(".json"));

        for (Identifier path : resources) {
            try {
                Resource resource = manager.getResource(path);
                try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    JsonElement json = new JsonParser().parse(reader);

                    Identifier identifier = identifierFromPath(path);

                    DataResult<GamePortalConfig> result = GamePortalConfig.CODEC.parse(JsonOps.INSTANCE, json);

                    result.result().ifPresent(config -> {
                        configs.put(identifier, config);
                    });

                    result.error().ifPresent(error -> {
                        Plasmid.LOGGER.error("Failed to parse game portal at {}: {}", path, error.toString());
                    });
                }
            } catch (IOException e) {
                Plasmid.LOGGER.error("Failed to read game portal at {}", path, e);
            }
        }

        return configs;
    }

    private static Identifier identifierFromPath(Identifier location) {
        String path = location.getPath();
        path = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return new Identifier(location.getNamespace(), path);
    }

    public void tick() {
        MinecraftServer server = this.server;
        if (server == null) {
            return;
        }

        long time = server.getOverworld().getTime();
        if (time - this.lastDisplayUpdateTime > DISPLAY_UPDATE_INTERVAL) {
            this.updatePortalDisplays();
            this.lastDisplayUpdateTime = time;
        }
    }

    private void updatePortalDisplays() {
        for (GamePortal portal : this.portals.values()) {
            portal.updateDisplay();
        }
    }

    public Set<Identifier> keySet() {
        return this.portals.keySet();
    }

    public Collection<GamePortal> getPortals() {
        return this.portals.values();
    }

    @Nullable
    public GamePortal byId(Identifier id) {
        return this.portals.get(id);
    }
}
