package xyz.nucleoid.plasmid.impl.portal;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface GamePortalBackend {
    default void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, Text.empty().append(this.getName()).formatted(Formatting.AQUA));
        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
    }

    void applyTo(ServerPlayerEntity player);

    default Text getName() {
        return Text.literal("༼ つ ◕_◕ ༽つ (Unnamed)");
    }

    default List<Text> getDescription() {
        return Collections.emptyList();
    }
    
    default ItemStack getIcon() {
        return new ItemStack(Items.GRASS_BLOCK);
    }

    default int getPlayerCount() {
        return -1;
    }

    default int getSpectatorCount() {
        return -1;
    }

    default ActionType getActionType() {
        return ActionType.NONE;
    }

    default void provideGameSpaces(Consumer<GameSpace> consumer) {}

    interface Factory {
        GamePortalBackend create(MinecraftServer server, Identifier id);
    }

    record ActionType(Text text) {
        public static ActionType NONE = new ActionType(Text.empty());
        public static ActionType PLAY = new ActionType(Text.translatable("text.plasmid.ui.game_join.action.play"));
        public static ActionType SPECTATE = new ActionType(Text.translatable("text.plasmid.ui.game_join.action.spectate"));
    }
}
