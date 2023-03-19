package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface GameConfigGamePortalBackend extends GamePortalBackend {
    Identifier gameId();

    @Override
    default void provideGameSpaces(Consumer<GameSpace> consumer) {
        var gameConfig = GameConfigs.get(this.gameId());
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(gameConfig)) {
                consumer.accept(gameSpace);
            }
        }
    }

    @Override
    default int getPlayerCount() {
        int count = 0;
        var gameConfig = GameConfigs.get(this.gameId());
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().isSourceConfig(gameConfig)) {
                count += gameSpace.getPlayers().size();
            }
        }
        return count;
    }

    @Override
    default List<Text> getDescription() {
        var config = GameConfigs.get(this.gameId());
        if (config != null) {
            return config.description();
        }

        return Collections.emptyList();
    }

    @Override
    default ItemStack getIcon() {
        var config = GameConfigs.get(this.gameId());
        if (config != null) {
            return config.icon();
        }

        return Items.BARRIER.getDefaultStack();
    }

    @Override
    default Text getName() {
        var config = GameConfigs.get(this.gameId());
        if (config != null) {
            return GameConfig.name(config);
        } else {
            return Text.literal(this.gameId().toString()).formatted(Formatting.RED);
        }
    }

    @Override
    default ActionType getActionType() {
        return ActionType.PLAY;
    }
}
