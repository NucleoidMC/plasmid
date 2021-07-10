package xyz.nucleoid.plasmid.error;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import xyz.nucleoid.plasmid.Plasmid;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class DiscordWebhook {
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("webhook-error-reporter")
            .setDaemon(true)
            .build()
    );

    private static final String BOUNDARY = Long.toHexString(System.nanoTime());
    private static final String CONTENT_TYPE = "multipart/form-data;boundary=\"" + BOUNDARY + "\"";

    private final String url;

    private DiscordWebhook(String url) {
        this.url = url;
    }

    public static DiscordWebhook open(String url) {
        return new DiscordWebhook(url);
    }

    public void post(Message message) {
        EXECUTOR.execute(() -> {
            try {
                var connection = (HttpsURLConnection) new URL(this.url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", CONTENT_TYPE);
                connection.setRequestProperty("User-Agent", "plasmid");

                try (var writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    message.writeTo(writer);
                }

                connection.getInputStream().close();
            } catch (IOException e) {
                Plasmid.LOGGER.error("Failed to post to discord webhook", e);
            }
        });
    }

    static class Message {
        private static final Gson GSON = new Gson();

        private final String content;
        private final List<File> files = new ArrayList<>();

        Message(String content) {
            this.content = content;
        }

        public Message addFile(String name, String content) {
            this.files.add(new File(name, content));
            return this;
        }

        void writeTo(PrintWriter writer) {
            writer.println("--" + BOUNDARY);

            var payload = new JsonObject();
            payload.addProperty("content", this.content);

            var allowedMentions = new JsonObject();
            allowedMentions.add("parse", new JsonArray());
            payload.add("allowed_mentions", allowedMentions);

            writer.println("--" + BOUNDARY);
            writer.println("Content-Disposition: form-data; name=\"payload_json\"");
            writer.println();
            writer.println(GSON.toJson(payload));

            for (var file : this.files) {
                writer.println("--" + BOUNDARY);
                writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.name + "\"");
                writer.println();
                writer.println(file.content);
            }

            writer.println("--" + BOUNDARY + "--");
        }
    }

    record File(String name, String content) {
    }
}
