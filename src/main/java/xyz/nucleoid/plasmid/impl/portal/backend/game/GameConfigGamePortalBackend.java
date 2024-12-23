package xyz.nucleoid.plasmid.impl.portal.backend.game;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;

import java.util.List;
import java.util.function.Consumer;

public interface GameConfigGamePortalBackend extends GamePortalBackend {
    RegistryEntry<GameConfig<?>> game();

    @Override
    default void provideGameSpaces(Consumer<GameSpace> consumer) {
        for (var gameSpace : GameSpaceManagerImpl.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(this.game())) {
                consumer.accept(gameSpace);
            }
        }
    }

    @Override
    default int getPlayerCount() {
        int count = 0;
        for (var gameSpace : GameSpaceManagerImpl.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(this.game())) {
                count += gameSpace.getState().players();
            }
        }
        return count;
    }

    @Override
    default int getSpectatorCount() {
        int count = 0;
        for (var gameSpace : GameSpaceManagerImpl.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(this.game())) {
                count += gameSpace.getState().spectators();
            }
        }
        return count;
    }

    @Override
    default List<Text> getDescription() {
        return this.game().value().description();
    }

    @Override
    default ItemStack getIcon() {
        return this.game().value().icon();
    }

    @Override
    default Text getName() {
        return GameConfig.name(this.game());
    }

    @Override
    default ActionType getActionType() {
        return ActionType.PLAY;
    }
}
