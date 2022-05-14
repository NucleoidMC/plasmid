package xyz.nucleoid.plasmid.game.portal;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

public interface GamePortalBackend {
    void populateDisplay(GamePortalDisplay display);

    CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player);

    default Text getName() {
        return new LiteralText("༼ つ ◕_◕ ༽つ (Unnamed)");
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
