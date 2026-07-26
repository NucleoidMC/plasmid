package xyz.nucleoid.plasmid.impl.portal.game;

import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface GameConfigGamePortalBackend extends GamePortalBackend {
    Holder<GameConfig<?>> game();

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
    default List<Component> getDescription() {
        return this.game().value().description();
    }

    @Override
    default ItemStack getIcon() {
        return this.game().value().icon();
    }

    @Override
    default Component getName() {
        return GameConfig.name(this.game());
    }

    @Override
    default ActionType getActionType() {
        return ActionType.PLAY;
    }
}
