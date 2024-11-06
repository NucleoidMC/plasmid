package xyz.nucleoid.plasmid.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public record PlasmidConfig(
        Optional<String> userFacingPackAddress,
        Optional<PlasmidWebServer.Config> webServerConfig
) {
    private static final Path PATH = Paths.get("config/plasmid.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Codec<PlasmidConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.STRING.optionalFieldOf("resource_pack_address").forGetter(PlasmidConfig::userFacingPackAddress),
                PlasmidWebServer.Config.CODEC.optionalFieldOf("web_server").forGetter(PlasmidConfig::webServerConfig)
        ).apply(instance, PlasmidConfig::new)
    );

    private static PlasmidConfig instance;

    private PlasmidConfig() {
        this(
                Optional.of("http://127.0.0.1:25566/" + PlasmidWebServer.RESOURCE_PACKS_ENDPOINT),
                Optional.of(new PlasmidWebServer.Config(25566))
        );
    }

    @NotNull
    public static PlasmidConfig get() {
        if (instance == null) {
            instance = initializeConfig();
        }
        return instance;
    }

    private static PlasmidConfig initializeConfig() {
        if (Files.exists(PATH)) {
            return loadConfig();
        } else {
            return createDefaultConfig();
        }
    }

    private static PlasmidConfig loadConfig() {
        try (var input = Files.newInputStream(PATH)) {
            var json = JsonParser.parseReader(new InputStreamReader(input));
            var result = CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
            return result.result().orElseGet(PlasmidConfig::new);
        } catch (IOException e) {
            Plasmid.LOGGER.warn("Failed to load Plasmid config", e);
            return new PlasmidConfig();
        }
    }

    private static PlasmidConfig createDefaultConfig() {
        var config = new PlasmidConfig();
        try (var output = Files.newOutputStream(PATH)) {
            var result = CODEC.encodeStart(JsonOps.INSTANCE, config).result();
            if (result.isPresent()) {
                var json = result.get();
                IOUtils.write(GSON.toJson(json), output, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Plasmid.LOGGER.warn("Failed to create default Plasmid config", e);
        }
        return config;
    }
}
