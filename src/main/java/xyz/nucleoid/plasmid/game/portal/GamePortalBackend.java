package xyz.nucleoid.plasmid.game.portal;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.ListedGameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GamePortalBackend {
    void populateDisplay(GamePortalDisplay display);

    CompletableFuture<ListedGameSpace> requestJoin(ServerPlayerEntity player);

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
        return 0;
    }

    @Nullable
    default GuiProvider getGuiProvider() {
        return null;
    }

    interface Factory {
        GamePortalBackend create(MinecraftServer server, Identifier id);
    }
}
