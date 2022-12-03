package xyz.nucleoid.plasmid.game.config;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ListedGameConfig {
    CompletableFuture<GameSpace> open(MinecraftServer server);

    Text name();

    default Text shortName() {
        return this.name();
    }

    default List<Text> description() {
        return List.of();
    }

    default ItemStack icon() {
        return new ItemStack(Items.BARRIER);
    }
}
