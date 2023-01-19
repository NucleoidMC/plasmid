package xyz.nucleoid.plasmid.game.portal.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.List;
import java.util.function.Consumer;

public interface MenuEntry {
    Text name();
    List<Text> description();
    ItemStack icon();

    void click(ServerPlayerEntity player);
    default int getPlayerCount() {
        return -1;
    }

    default void provideGameSpaces(Consumer<GameSpace> consumer) {

    }
}