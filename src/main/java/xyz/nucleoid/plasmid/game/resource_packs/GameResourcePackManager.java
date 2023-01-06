package xyz.nucleoid.plasmid.game.resource_packs;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.PlasmidConfig;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manager for all resource packs tracked by the server.
 *
 * @see GameResourcePack
 */
public final class GameResourcePackManager {
    private static final Path ROOT = FabricLoader.getInstance().getGameDir()
            .resolve("plasmid-generated/resource-packs");

    private static final Path TEMPORARY = ROOT.resolve("tmp.zip");

    private static GameResourcePackManager instance;

    private final String packEndpoint;

    private final Map<String, ServedPack> hashToServedPack = new Object2ObjectOpenHashMap<>();
    private GameResourcePack emptyPack;

    private GameResourcePackManager(String packEndpoint) {
        this.packEndpoint = packEndpoint;
    }

    public static Optional<GameResourcePackManager> get() {
        var instance = GameResourcePackManager.instance;
        if (instance == null) {
            var packEndpoint = PlasmidConfig.get().userFacingPackAddress();
            if (packEndpoint.isPresent()) {
                GameResourcePackManager.instance = instance = new GameResourcePackManager(packEndpoint.get());
            }
        }

        return Optional.ofNullable(instance);
    }

    public static Optional<GameResourcePack> emptyPack() {
        return get().flatMap(packs -> {
            try {
                return Optional.of(packs.getEmptyPack());
            } catch (Exception e) {
                Plasmid.LOGGER.error("Failed to build empty resource pack", e);
                return Optional.empty();
            }
        });
    }

    @Nullable
    public GameResourcePackManager.ServedPack load(String query) {
        var pack = this.hashToServedPack.get(query);
        if (pack == null) {
            return null;
        }

        if (!Files.exists(pack.path)) {
            this.hashToServedPack.remove(query);
            return null;
        }

        return pack;
    }

    /**
     * Generates resource pack from Polymer's {@link ResourcePackCreator}
     * Created GameResourcePack instance should be stored and used for multiple GameActivities
     *
     * @param creator Polymer {@link ResourcePackCreator}
     * @return Instance of {@link GameResourcePack}
     */
    public GameResourcePack register(ResourcePackCreator creator) throws Exception {
        return this.register(creator::build);
    }

    public GameResourcePack register(Builder builder) throws Exception {
        Files.createDirectories(ROOT);

        synchronized (TEMPORARY) {
            Files.deleteIfExists(TEMPORARY);
            builder.accept(TEMPORARY);

            var hash = hash(TEMPORARY);
            var path = this.getPathFor(hash);

            Files.move(TEMPORARY, path, StandardCopyOption.REPLACE_EXISTING);

            var pack = new ServedPack(path, Files.size(path));
            this.hashToServedPack.put(hash, pack);

            return new GameResourcePack(this.getUrlFor(hash), hash);
        }
    }

    private String getUrlFor(String hash) {
        return this.packEndpoint + "/" + hash;
    }

    public GameResourcePack getEmptyPack() throws Exception {
        var emptyPack = this.emptyPack;
        if (emptyPack == null) {
            this.emptyPack = emptyPack = this.register(this::buildEmptyPack);
        }

        return emptyPack;
    }

    private Path getPathFor(String hash) {
        return ROOT.resolve(hash + ".zip");
    }

    private void buildEmptyPack(Path path) throws Exception {
        try (var output = new ZipOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            var json = new JsonObject();
            json.add("pack", Util.make(() -> {
                var pack = new JsonObject();
                pack.addProperty("pack_format", SharedConstants.RESOURCE_PACK_VERSION);
                pack.addProperty("description", "Empty Resource Pack");
                return pack;
            }));

            var entry = new ZipEntry("pack.mcmeta");
            entry.setTime(0);
            output.putNextEntry(entry);
            output.write(json.toString().getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
    }

    private static String hash(Path path) throws IOException {
        return com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha1()).toString();
    }

    public interface Builder {
        void accept(Path path) throws Exception;
    }

    public static final class ServedPack {
        private final Path path;
        private final long size;

        private ServedPack(Path path, long size) {
            this.path = path;
            this.size = size;
        }

        public InputStream openInputStream() throws IOException {
            return Files.newInputStream(this.path);
        }

        public long getSize() {
            return this.size;
        }
    }
}
