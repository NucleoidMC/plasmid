package xyz.nucleoid.plasmid.game.portal;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.IOException;
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

    public void setup(MinecraftServer server) {
        this.server = server;

        var queue = this.portalQueue;
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

    @ApiStatus.Internal
    public void reload(DynamicRegistryManager registryManager, ResourceManager manager) {
        this.portals.clear();

        var configs = this.loadConfigs(registryManager, manager);
        if (this.server != null) {
            this.loadPortalsFrom(this.server, configs);
        } else {
            this.portalQueue = configs;
        }
    }

    private void loadPortalsFrom(MinecraftServer server, Map<Identifier, GamePortalConfig> queue) {
        for (var entry : queue.entrySet()) {
            var identifier = entry.getKey();
            var config = entry.getValue();

            var portal = new GamePortal(server, identifier, config::createBackend);
            portal.setCustom(config.custom());

            this.portals.register(identifier, portal);
        }
    }

    private Map<Identifier, GamePortalConfig> loadConfigs(DynamicRegistryManager registryManager, ResourceManager manager) {
        var configs = new Object2ObjectOpenHashMap<Identifier, GamePortalConfig>();

        manager.findResources(PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    var json = JsonParser.parseReader(reader);
                    var identifier = identifierFromPath(path);

                    var result = GamePortalConfig.CODEC.parse(RegistryOps.of(JsonOps.INSTANCE, registryManager), json);

                    result.result().ifPresent(config -> {
                        configs.put(identifier, config);
                    });

                    result.error().ifPresent(error -> {
                        Plasmid.LOGGER.error("Failed to parse game portal at {}: {}", path, error.toString());
                    });
                }
            } catch (IOException e) {
                Plasmid.LOGGER.error("Failed to read game portal at {}", path, e);
            } catch (JsonParseException e) {
                Plasmid.LOGGER.error("Failed to parse game portal JSON at {}: {}", path, e);
            }
        });

        return configs;
    }

    private static Identifier identifierFromPath(Identifier location) {
        var path = location.getPath();
        path = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return new Identifier(location.getNamespace(), path);
    }

    public void tick() {
        var server = this.server;
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
        for (var portal : this.portals.values()) {
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
