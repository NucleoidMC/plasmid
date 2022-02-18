package xyz.nucleoid.plasmid;

import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.resource_packs.GameResourcePackManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class PlasmidWebServer {
    public static final String RESOURCE_PACKS_ENDPOINT = "resource-packs";

    @Nullable
    public static HttpServer start(MinecraftServer minecraftServer, Config config) {
        try {
            var address = createBindAddress(minecraftServer, config);
            var server = HttpServer.create(address, 0);
            server.createContext("/" + RESOURCE_PACKS_ENDPOINT, new ResourcePacksHandler(RESOURCE_PACKS_ENDPOINT));
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            Plasmid.LOGGER.info("Web server started at: " + address);
            return server;
        } catch (IOException e) {
            Plasmid.LOGGER.error("Failed to start the web server!", e);
            e.printStackTrace();
            return null;
        }
    }

    private static InetSocketAddress createBindAddress(MinecraftServer server, Config config) {
        var serverIp = server.getServerIp();
        if (!Strings.isNullOrEmpty(serverIp)) {
            return new InetSocketAddress(serverIp, config.port());
        } else {
            return new InetSocketAddress(config.port());
        }
    }

    private record ResourcePacksHandler(String endpoint) implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (exchange) {
                var resourcePacks = GameResourcePackManager.get();
                if (resourcePacks.isEmpty() || !this.tryHandle(exchange, resourcePacks.get())) {
                    exchange.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
                }
            }
        }

        private boolean tryHandle(HttpExchange exchange, GameResourcePackManager resourcePacks) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                return this.tryHandleGet(exchange, resourcePacks);
            }
            return false;
        }

        private boolean tryHandleGet(HttpExchange exchange, GameResourcePackManager resourcePacks) throws IOException {
            var path = exchange.getRequestURI().getPath().substring(this.endpoint.length() + 2);
            var pack = resourcePacks.load(path);
            if (pack != null) {
                try (
                        var input = pack.openInputStream();
                        var output = exchange.getResponseBody()
                ) {
                    exchange.getResponseHeaders().add("Server", "plasmid");
                    exchange.getResponseHeaders().add("Content-Type", "application/zip");
                    exchange.sendResponseHeaders(HttpStatus.SC_OK, pack.getSize());

                    IOUtils.copy(input, output);
                    output.flush();

                    return true;
                }
            }

            return false;
        }
    }

    public record Config(int port) {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("port").forGetter(Config::port)
                ).apply(instance, Config::new)
        );
    }
}
