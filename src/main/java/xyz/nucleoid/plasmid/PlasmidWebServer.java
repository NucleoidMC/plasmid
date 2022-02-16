package xyz.nucleoid.plasmid;

import com.google.common.io.Files;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;

public class PlasmidWebServer implements HttpHandler {
    private static final Path BASE_PATH = FabricLoader.getInstance().getGameDir().resolve("plasmid-generated");

    @Nullable
    public static HttpServer start(MinecraftServer minecraftServer) {
        try {
            var config = PlasmidConfig.get();
            String serverIp = minecraftServer.getServerIp();
            if (serverIp == null || serverIp.isEmpty()) {
                serverIp = InetAddress.getLocalHost().getHostAddress();
            }
            var address = new InetSocketAddress(serverIp, config.webServerConfig().serverPort());
            var server = HttpServer.create(address, 0);
            server.createContext("/", new PlasmidWebServer());
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            Plasmid.LOGGER.info("Web server started at: " + new InetSocketAddress(serverIp, config.webServerConfig().serverPort()));
            return server;
        } catch (IOException e) {
            Plasmid.LOGGER.error("Failed to start the web server!", e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Objects.equals(exchange.getRequestMethod(), "GET")) {
            File file = BASE_PATH.resolve(exchange.getRequestURI().normalize().getPath().substring(1).replace("../", "")).toFile();

            if (file.exists()) {
                var outputStream = exchange.getResponseBody();

                exchange.getResponseHeaders().add("User-Agent", "Server: plasmid");
                exchange.sendResponseHeaders(200, file.length());

                Files.copy(file, outputStream);

                outputStream.flush();
                outputStream.close();
                exchange.close();
                return;
            }
        }

        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }

    public record Config(
            boolean enabled,
            int serverPort
    ) {
        protected static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("enabled", false).forGetter(Config::enabled),
                        Codec.INT.optionalFieldOf("port", 25566).forGetter(Config::serverPort)
                ).apply(instance, Config::new)
        );

        public static Config createDefault() {
            return new Config(false, 25566);
        }
    }
}