package xyz.nucleoid.plasmid;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executors;

public class PlasmidWebServer implements HttpHandler {
    @Nullable
    public static HttpServer start(MinecraftServer minecraftServer) {
        try {
            var config = PlasmidConfig.get();
            String serverIp = minecraftServer.getServerIp();
            if (serverIp == null || serverIp.isEmpty()) {
                serverIp = InetAddress.getLocalHost().getHostAddress();
            }
            var address = new InetSocketAddress(serverIp, config.serverPort());
            var server = HttpServer.create(address, 0);
            server.createContext("/", new PlasmidWebServer());
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            Plasmid.LOGGER.info("Web server started at: " + new InetSocketAddress(serverIp, config.serverPort()));
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
            File pack = GameResourcePack.BASE_PATH.resolve(exchange.getRequestURI().normalize().getPath().substring(1)).toFile();

            if (pack.exists()) {
                var outputStream = exchange.getResponseBody();

                exchange.getResponseHeaders().add("User-Agent", "Java/plasmid");
                exchange.sendResponseHeaders(200, pack.length());

                var fis = new FileInputStream(pack);
                int b;
                while ((b = fis.read()) != -1) {
                    outputStream.write(b);
                }
                fis.close();

                outputStream.flush();
                outputStream.close();
                exchange.close();
                return;
            }
        }

        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }
}