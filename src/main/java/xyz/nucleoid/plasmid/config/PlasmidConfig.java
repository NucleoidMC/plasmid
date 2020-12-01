package xyz.nucleoid.plasmid.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import joptsimple.internal.Strings;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PlasmidConfig {
    private static final Path PATH = Paths.get("config/plasmid.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static PlasmidConfig instance;

    @SerializedName("error_reporting_webhook_url")
    private String errorReportingWebhookUrl = "";

    @Nullable
    public String getErrorReportingWebhookUrl() {
        if (Strings.isNullOrEmpty(this.errorReportingWebhookUrl)) {
            return null;
        }
        return this.errorReportingWebhookUrl;
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
        try (InputStream input = Files.newInputStream(PATH)) {
            return GSON.fromJson(new InputStreamReader(input), PlasmidConfig.class);
        } catch (IOException e) {
            Plasmid.LOGGER.warn("Failed to load plasmid config", e);
            return new PlasmidConfig();
        }
    }

    private static PlasmidConfig createDefaultConfig() {
        PlasmidConfig config = new PlasmidConfig();
        try (OutputStream output = Files.newOutputStream(PATH)) {
            IOUtils.write(GSON.toJson(config), output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Plasmid.LOGGER.warn("Failed to create default plasmid config", e);
        }
        return config;
    }
}
