package xyz.nucleoid.plasmid.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.registry.TinyEntry;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public final class GameConfigs {
    private static final ResourceFinder FINDER = ResourceFinder.json("games");

    private static final TinyRegistry<GameConfig<?>> CONFIGS = TinyRegistry.create("games");

    public static Codec<TinyEntry<TinyRegistry.EntryKey<?>, Collection<GameConfig<?>>>> getEntryCodec() {
        return CONFIGS.getEntryCodec();
    }
    public static void reload(DynamicRegistryManager registryManager, ResourceManager manager) {
        CONFIGS.clear();

        DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryManager);

        FINDER.findResources(manager).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = FINDER.toResourceId(path);

                    Codec<GameConfig<?>> codec = GameConfig.codecFrom(identifier);
                    DataResult<GameConfig<?>> result = codec.parse(ops, json);

                    result.result().ifPresent(game -> {
                        CONFIGS.register(identifier, game);
                    });

                    result.error().ifPresent(error -> {
                        Plasmid.LOGGER.error("Failed to parse game at {}: {}", path, error);
                    });
                }
            } catch (IOException e) {
                Plasmid.LOGGER.error("Failed to read configured game at {}", path, e);
            } catch (JsonParseException e) {
                Plasmid.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
            }
        });

        CONFIGS.loadTags(manager);
    }

    @Nullable
    public static GameConfig<?> get(Identifier identifier) {
        return CONFIGS.get(identifier);
    }

    public static Set<Identifier> getKeys() {
        return CONFIGS.keySet();
    }
}
