package xyz.nucleoid.plasmid.impl.portal.backend.game;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;

public record NewGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(PortalUserContext context, ClickType clickType) {
        context.tryOpening(this.game);
    }
}
