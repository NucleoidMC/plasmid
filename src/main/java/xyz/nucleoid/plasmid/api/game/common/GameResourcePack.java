package xyz.nucleoid.plasmid.api.game.common;

import com.google.common.hash.Hashing;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.PlasmidWebServer;
import xyz.nucleoid.plasmid.api.game.GameActivity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * A very simple utility to apply a resource pack for all players within a {@link GameActivity}.
 *
 * @see GameResourcePack#addTo(GameActivity)
 */
public final class GameResourcePack {
    private static final Path RESOURCE_PACKS_ROOT = FabricLoader.getInstance().getGameDir()
            .resolve("plasmid-generated/resource-packs");
    private final UUID uuid;

    private final String url;
    private final String hash;
    private boolean required;
    private Component prompt;

    private boolean isLocal;

    public GameResourcePack(String url, String hash) {
        this.url = url;
        this.uuid = UUID.nameUUIDFromBytes(hash.getBytes());
        this.hash = hash;
        this.isLocal = false;
    }

    public GameResourcePack(UUID uuid, String url, String hash) {
        this.url = url;
        this.uuid = uuid;
        this.hash = hash;
        this.isLocal = false;
    }

    private GameResourcePack(UUID uuid, String url, String hash, Void unused) {
        this.url = url;
        this.uuid = uuid;
        this.hash = hash;
        this.isLocal = true;
    }

    public boolean isLocal() {
        return this.isLocal;
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
    public GameResourcePack setPrompt(Component prompt) {
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
        activity.listen(GamePlayerEvents.ADD, this::sendTo);
        activity.listen(GamePlayerEvents.REMOVE, this::unload);
    }

    private void sendTo(ServerPlayer player) {
        player.connection.send(new ClientboundResourcePackPushPacket(this.uuid, this.url, this.hash, this.required, Optional.ofNullable(this.prompt)));
    }

    private void unload(ServerPlayer player) {
        player.connection.send(new ClientboundResourcePackPopPacket(Optional.of(this.uuid)));
    }

    public static Optional<GameResourcePack> from(Identifier identifier, ResourcePackCreator creator) {
        try {
            var relative = identifier.getNamespace() + "/" + identifier.getPath() + ".zip";
            var path = RESOURCE_PACKS_ROOT.resolve(relative);
            Files.createDirectories(path.getParent());
            var res = creator.build(path);
            var url = PlasmidWebServer.registerResourcePack(relative, path);
            return Optional.of(new GameResourcePack(UUID.nameUUIDFromBytes(res.hash().getBytes(StandardCharsets.UTF_8)), url, res.hash(), null));
        } catch (Throwable e) {
            Plasmid.LOGGER.error("Failed to create a resource pack '" + identifier + "'!", e);
            return Optional.empty();
        }
    }
}
