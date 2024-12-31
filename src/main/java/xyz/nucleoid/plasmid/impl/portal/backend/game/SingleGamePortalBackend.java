package xyz.nucleoid.plasmid.impl.portal.backend.game;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;

public record SingleGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(PortalUserContext context, ClickType clickType) {
        var x = GameSpaceManagerImpl.get().getOpenGameSpaces();

        if (!x.isEmpty() && context.canJoinExisting()) {
            for (var gameSpace : x) {
                if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                    var result = context.tryJoin(gameSpace, JoinIntent.PLAY);

                    if (result.isOk()) {
                        return;
                    }
                }
            }
        } else {
            context.tryOpening(this.game);
        }
    }
}
