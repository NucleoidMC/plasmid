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
    private Map<UUID, GameResourcePack> resourcePacks = new HashMap<>();
    private Map<UUID, GameResourcePack> oldResourcePacks = new HashMap<>();
    private boolean active = false;

    @ApiStatus.Internal
    public ResourcePackStates(GameSpace gameSpace) {
        gameSpace.getLifecycle().addListeners(new GameLifecycle.Listeners() {
            @Override
            public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
                ResourcePackStates.this.setFor(player, null);
            }

            @Override
            public void beforeActivityChange(GameSpace gameSpace, GameActivity newActivity, @Nullable GameActivity oldActivity) {
                ResourcePackStates.this.oldResourcePacks = ResourcePackStates.this.resourcePacks;
                ResourcePackStates.this.resourcePacks = new HashMap<>();

                ResourcePackStates.this.active = false;
            }

            @Override
            public void afterActivityChange(GameSpace gameSpace, GameActivity newActivity, @Nullable GameActivity oldActivity) {
                var oldPacks = ResourcePackStates.this.oldResourcePacks;
                var newPacks = ResourcePackStates.this.resourcePacks;

                for (var player : gameSpace.getPlayers()) {
                    var previousPack = oldPacks.get(player.getUuid());
                    var currentPack = newPacks.get(player.getUuid());

                    if (currentPack != previousPack) {
                        if (currentPack == null) {
                            resetPack(player);
                        } else {
                            currentPack.sendTo(player);
                        }
                    }
                }
                ResourcePackStates.this.active = true;
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
            if (this.active && oldPack != null && !player.isDisconnected()) {
                resetPack(player);
            }
        } else if (this.getFor(player) != pack) {
            this.resourcePacks.put(player.getUuid(), pack);
            if (this.active) {
                pack.sendTo(player);
            }
        }
    }
}
