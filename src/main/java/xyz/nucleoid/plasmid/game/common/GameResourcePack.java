package xyz.nucleoid.plasmid.game.common;

import joptsimple.internal.Strings;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

// TODO: prevent resource-pack soft-lock

/**
 * A very simple utility to apply a resource pack for all players within a {@link GameActivity}.
 *
 * @see GameResourcePack#addTo(GameActivity)
 */
public final class GameResourcePack {
    private static final String EMPTY_PACK_URL = "https://nucleoid.xyz/resources/empty_resource_pack.zip";
    private static final String EMPTY_PACK_HASH = "B740E5E6C39C0549D05A1F979156B1FE6A03D9BF";

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
}
