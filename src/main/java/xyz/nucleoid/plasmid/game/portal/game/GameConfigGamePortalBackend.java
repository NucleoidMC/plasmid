package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface GameConfigGamePortalBackend extends GamePortalBackend {
    RegistryEntry<GameConfig<?>> game();

    @Override
    default void provideGameSpaces(Consumer<GameSpace> consumer) {
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(this.game())) {
                consumer.accept(gameSpace);
            }
        }
    }

    @Override
    default int getPlayerCount() {
        int count = 0;
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(this.game())) {
                count += gameSpace.getPlayers().size();
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
