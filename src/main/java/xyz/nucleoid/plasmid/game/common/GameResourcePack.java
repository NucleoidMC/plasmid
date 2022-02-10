package xyz.nucleoid.plasmid.game.common;

import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import joptsimple.internal.Strings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.PlasmidConfig;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.MessageDigest;

// TODO: prevent resource-pack soft-lock

/**
 * A very simple utility to apply a resource pack for all players within a {@link GameActivity}.
 *
 * @see GameResourcePack#addTo(GameActivity)
 */
public final class GameResourcePack {
    private static final String EMPTY_PACK_URL = "https://nucleoid.xyz/resources/empty_resource_pack.zip";
    private static final String EMPTY_PACK_HASH = "B740E5E6C39C0549D05A1F979156B1FE6A03D9BF";
    private static final GameResourcePack EMPTY = new GameResourcePack(EMPTY_PACK_URL, EMPTY_PACK_HASH);
    public static final Path BASE_PATH = FabricLoader.getInstance().getGameDir().resolve("plasmid-generated").resolve("resource-packs");

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
        var server = activity.getGameSpace().getServer();
        var serverUrl = server.getResourcePackUrl();
        var serverHash = server.getResourcePackHash();

        activity.listen(GamePlayerEvents.ADD, player -> {
            player.sendResourcePackUrl(this.url, this.hash, this.required, this.prompt);
        });

        activity.listen(GamePlayerEvents.REMOVE, player -> {
            if (!Strings.isNullOrEmpty(serverUrl) && !Strings.isNullOrEmpty(serverHash)) {
                player.sendResourcePackUrl(serverUrl, serverHash, true, null);
            } else {
                player.sendResourcePackUrl(EMPTY_PACK_URL, EMPTY_PACK_HASH, true, null);
            }
        });
    }

    /**
     * Generates resource pack from Polymer's {@link ResourcePackCreator}
     * Created GameResourcePack instance should be stored and used for multiple GameActivities
     *
     * @param identifier Unique {@link Identifier}
     * @param creator Polymer {@link ResourcePackCreator}
     * @return Instance of {@link GameResourcePack}
     */
    public static GameResourcePack generate(Identifier identifier, ResourcePackCreator creator) {
        var file = BASE_PATH.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        String sha1;
        try {
            var packPath = BASE_PATH.resolve(identifier.getNamespace()).resolve(identifier.getPath() + ".zip");
            creator.build(packPath);
            sha1 = createSha1(packPath.toFile());
        } catch (Exception e) {
            Plasmid.LOGGER.error("Couldn't generate resource pack " + identifier.toString(), e);
            return EMPTY;
        }

        return new GameResourcePack(PlasmidConfig.get().userFacingPackAddress() + "/" + identifier.getNamespace() + "/" + identifier.getPath() + ".zip", sha1);
    }

    private static String createSha1(File file) throws Exception  {
        var digest = MessageDigest.getInstance("SHA-1");
        var fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }

        var bytes = digest.digest();

        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return sb.toString();
    }
}
