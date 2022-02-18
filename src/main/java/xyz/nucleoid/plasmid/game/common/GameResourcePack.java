package xyz.nucleoid.plasmid.game.common;

import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.resource_packs.GameResourcePackManager;

import java.util.Optional;

// TODO: prevent resource-pack soft-lock

/**
 * A very simple utility to apply a resource pack for all players within a {@link GameActivity}.
 *
 * @see GameResourcePack#addTo(GameActivity)
 */
public final class GameResourcePack {
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

    public static Optional<GameResourcePack> tryRegister(ResourcePackCreator creator) {
        return GameResourcePackManager.get().flatMap(packs -> {
            try {
                return Optional.of(packs.register(creator));
            } catch (Exception e) {
                Plasmid.LOGGER.error("Failed to generate resource pack", e);
                return Optional.empty();
            }
        });
    }
}
