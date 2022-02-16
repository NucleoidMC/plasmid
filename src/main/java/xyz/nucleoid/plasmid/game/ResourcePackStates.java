package xyz.nucleoid.plasmid.game;

import joptsimple.internal.Strings;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ResourcePackStates {
    private final Map<UUID, GameResourcePack> resourcePacks = new HashMap<>();
    private GameResourcePack globalPack = null;

    @ApiStatus.Internal
    public ResourcePackStates(GameSpace gameSpace) {
        gameSpace.getLifecycle().addListeners(new GameLifecycle.Listeners() {
            @Override
            public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
                ResourcePackStates.this.setFor(player, null);
            }

            @Override
            public void onActivityChange(GameSpace gameSpace, GameActivity newActivity, @Nullable GameActivity oldActivity) {
                var packs = ResourcePackStates.this.resourcePacks;
                var globalPack = ResourcePackStates.this.globalPack;

                for (var player : gameSpace.getPlayers()) {
                    var currentPack = packs.get(player.getUuid());

                    if (currentPack != globalPack && globalPack == null) {
                        resetPack(player);
                    }
                }
            }
        });
    }

    private static void resetPack(ServerPlayerEntity player) {
        var serverUrl = player.getServer().getResourcePackUrl();
        var serverHash = player.getServer().getResourcePackHash();

        if (!Strings.isNullOrEmpty(serverUrl) && !Strings.isNullOrEmpty(serverHash)) {
            player.sendResourcePackUrl(serverUrl, serverHash, true, player.getServer().getResourcePackPrompt());
        } else {
            GameResourcePack.EMPTY.sendTo(player);
        }
    }

    /**
     * Gets player's current {@link GameResourcePack}
     *
     * @param player player
     * @return {@link GameResourcePack} or null
     */
    @Nullable
    public GameResourcePack getFor(ServerPlayerEntity player) {
        return this.resourcePacks.get(player.getUuid());
    }

    /**
     * Sets resource pack state for player, used to limit resource pack reloads on activity changes
     *
     * @param player
     * @param pack {@link GameResourcePack}'s of player
     */
    public void setFor(ServerPlayerEntity player, GameResourcePack pack) {
        if (pack == null) {
            var oldPack = this.resourcePacks.remove(player.getUuid());
            if (oldPack != null && !player.isDisconnected()) {
                resetPack(player);
            }
        } else if (this.getFor(player) != pack) {
            this.resourcePacks.put(player.getUuid(), pack);
            pack.sendTo(player);
        }
    }

    @ApiStatus.Internal
    public void setCurrentGlobalPack(GameResourcePack pack) {
        this.globalPack = pack;
    }
}
