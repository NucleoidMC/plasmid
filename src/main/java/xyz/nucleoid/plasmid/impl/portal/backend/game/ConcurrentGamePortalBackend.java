package xyz.nucleoid.plasmid.impl.portal.backend.game;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;

public record ConcurrentGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(PortalUserContext context, ClickType type) {
        if (context.canJoinExisting()) {
            for (var gameSpace : GameSpaceManagerImpl.get().getOpenGameSpaces()) {
                if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                    var result = context.tryJoin(gameSpace, JoinIntent.PLAY);

                    if (result.isOk()) {
                        return;
                    }
                }
            }
        }

        context.tryOpening(this.game);
    }
}
