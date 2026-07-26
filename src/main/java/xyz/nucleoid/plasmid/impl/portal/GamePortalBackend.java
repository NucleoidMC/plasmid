package xyz.nucleoid.plasmid.impl.portal;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface GamePortalBackend {
    default void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, Component.empty().append(this.getName()).withStyle(ChatFormatting.AQUA));
        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
        display.set(GamePortalDisplay.MAX_PLAYER_COUNT, this.getMaxPlayerCount());
        display.set(GamePortalDisplay.SPECTATOR_COUNT, this.getSpectatorCount());
    }

    void applyTo(ServerPlayer player, boolean alt);

    default Component getName() {
        return Component.literal("༼ つ ◕_◕ ༽つ (Unnamed)");
    }

    default List<Component> getDescription() {
        return Collections.emptyList();
    }
    
    default ItemStack getIcon() {
        return new ItemStack(Items.GRASS_BLOCK);
    }

    default int getPlayerCount() {
        return -1;
    }

    default int getMaxPlayerCount() {
        return -1;
    }

    default int getSpectatorCount() {
        return -1;
    }

    default ActionType getActionType() {
        return ActionType.NONE;
    }
    default ActionType getAltActionType() {
        return ActionType.NONE;
    }

    default void provideGameSpaces(Consumer<GameSpace> consumer) {}

    interface Factory {
        GamePortalBackend create(MinecraftServer server, Identifier id);
    }

    record ActionType(Component text, Component textAlt) {
        public static ActionType NONE = new ActionType(Component.empty(), Component.empty());
        public static ActionType PLAY = new ActionType(Component.translatable("text.plasmid.ui.game_join.action.play"), Component.translatable("text.plasmid.ui.game_join.action.play.alt"));
        public static ActionType SPECTATE = new ActionType(Component.translatable("text.plasmid.ui.game_join.action.spectate"), Component.translatable("text.plasmid.ui.game_join.action.spectate.alt"));
    }
}
