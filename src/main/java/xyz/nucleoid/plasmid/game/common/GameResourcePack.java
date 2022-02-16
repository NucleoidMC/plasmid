package xyz.nucleoid.plasmid.game.common;

import com.google.common.hash.Hashing;
import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.PlasmidConfig;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// TODO: prevent resource-pack soft-lock

/**
 * A very simple utility to apply a resource pack for all players within a {@link GameActivity}.
 *
 * @see GameResourcePack#addTo(GameActivity)
 */
public final class GameResourcePack {
    public static final Path BASE_PATH = FabricLoader.getInstance().getGameDir().resolve("plasmid-generated").resolve("resource-packs");
    public static final GameResourcePack EMPTY;
    private final String url;
    private final String hash;
    private boolean required;
    private Text prompt;

    public GameResourcePack(String url, String hash) {
        this.url = url;
        this.hash = hash;
    }

    /**
     * Indicates to clients that this resource pack is required and cannot be rejected.
     *
     * @return this {@link GameResourcePack} instance
     */
    public GameResourcePack setRequired() {
        this.required = true;
        return this;
    }

    /**
     * Sets a message to display to players when prompted to accept the resource pack.
     *
     * @return this {@link GameResourcePack} instance
     */
    public GameResourcePack setPrompt(Text prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Applies this resource pack to the given {@link GameActivity}.
     * Any player added to the {@link GameActivity} will be sent over this resource pack.
     *
     * @param activity the activity to add to
     */
    public void addTo(GameActivity activity) {
        var gameSpace = activity.getGameSpace();

        activity.listen(GamePlayerEvents.ADD, player -> {
            gameSpace.getResourcePackStates().setFor(player, this);
        });
    }

    @ApiStatus.Internal
    public void sendTo(ServerPlayerEntity player) {
        player.sendResourcePackUrl(this.url, this.hash, this.required, this.prompt);
    }

    /**
     * Generates resource pack from Polymer's {@link ResourcePackCreator}
     * Created GameResourcePack instance should be stored and used for multiple GameActivities
     *
     * @param identifier Unique {@link Identifier}
     * @param creator Polymer {@link ResourcePackCreator}
     * @return Instance of {@link GameResourcePack}
     */
    public static GameResourcePack create(Identifier identifier, ResourcePackCreator creator) {
        String sha1;
        try {
            Files.createDirectories(BASE_PATH);
            var packPath = BASE_PATH.resolve(identifier.getNamespace()).resolve(identifier.getPath() + ".zip");
            creator.build(packPath);
            sha1 = createSha1(packPath.toFile());
        } catch (Exception e) {
            Plasmid.LOGGER.error("Couldn't generate resource pack " + identifier.toString(), e);
            return EMPTY;
        }

        return new GameResourcePack(PlasmidConfig.get().userFacingPackAddress() + "/resource-packs/" + identifier.getNamespace() + "/" + identifier.getPath() + ".zip?" + sha1, sha1);
    }

    private static String createSha1(File file) throws Exception {
        return com.google.common.io.Files.asByteSource(file).hash(Hashing.sha1()).toString();
    }

    static {
        GameResourcePack emptyPack;

        try {
            var path = BASE_PATH.resolve("empty.zip");
            var outputStream = new ZipOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));


            var entry = new ZipEntry("pack.mcmeta");
            entry.setTime(0);
            outputStream.putNextEntry(entry);
            outputStream.write(("{" +
                    "   \"pack\":{" +
                    "      \"pack_format\":" + SharedConstants.RESOURCE_PACK_VERSION + "," +
                    "      \"description\":\"Empty Resource Pack\"" +
                    "   }\n" +
                    "}\n").getBytes(StandardCharsets.UTF_8));

            outputStream.closeEntry();
            outputStream.close();

            var sha1 = createSha1(path.toFile());

            emptyPack = new GameResourcePack(PlasmidConfig.get().userFacingPackAddress() + "/resource-packs/empty.zip?" + sha1, sha1);
        } catch (Exception e) {
            Plasmid.LOGGER.error("Couldn't generate empty resource pack!", e);
            emptyPack = new GameResourcePack("", "");
        }

        EMPTY = emptyPack;
    }
}
