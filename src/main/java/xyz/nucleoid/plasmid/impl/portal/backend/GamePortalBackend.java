package xyz.nucleoid.plasmid.impl.portal.backend;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.config.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalDisplay;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface GamePortalBackend {
    default void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, Text.empty().append(this.getName()).formatted(Formatting.AQUA));
        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
        display.set(GamePortalDisplay.MAX_PLAYER_COUNT, this.getMaxPlayerCount());
        display.set(GamePortalDisplay.SPECTATOR_COUNT, this.getSpectatorCount());
    }

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

    default int getMaxPlayerCount() {
        return -1;
    }

    default int getSpectatorCount() {
        return -1;
    }

    void applyTo(PortalUserContext context, ClickType clickType);

    default List<Action> getActions(PortalUserContext context) {
        return List.of();
    }
    default void provideGameSpaces(Consumer<GameSpace> consumer) {}

    interface Factory<T extends GamePortalConfig> {
        GamePortalBackend create(MinecraftServer server, Identifier id, T config);
    }

    record Action(Text text, Text textAlt) {
        public static Action NONE = new Action(Text.empty(), Text.empty());
        public static Action PLAY = new Action(Text.translatable("text.plasmid.ui.game_join.action.play"), Text.translatable("text.plasmid.ui.game_join.action.play.alt"));
        public static Action SPECTATE = new Action(Text.translatable("text.plasmid.ui.game_join.action.spectate"), Text.translatable("text.plasmid.ui.game_join.action.spectate.alt"));
    }
}
