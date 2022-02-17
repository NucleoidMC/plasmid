package xyz.nucleoid.plasmid.game.resource_packs;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import joptsimple.internal.Strings;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.util.Map;
import java.util.UUID;

public final class ResourcePackStates {
    private Map<UUID, GameResourcePack> resourcePacks = new Object2ObjectOpenHashMap<>();
    private Map<UUID, GameResourcePack> oldResourcePacks = new Object2ObjectOpenHashMap<>();
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
                ResourcePackStates.this.resourcePacks = new Object2ObjectOpenHashMap<>();

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
        var server = player.getServer();
        var serverUrl = server.getResourcePackUrl();
        var serverHash = server.getResourcePackHash();

        if (!Strings.isNullOrEmpty(serverUrl) && !Strings.isNullOrEmpty(serverHash)) {
            player.sendResourcePackUrl(serverUrl, serverHash, true, server.getResourcePackPrompt());
        } else {
            GameResourcePackManager.emptyPack().ifPresent(pack -> pack.sendTo(player));
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
     * @param player the player to set the resource pack for
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
