package xyz.nucleoid.plasmid.game.portal.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.ListedGameSpace;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MenuEntry {
    public Text name();
    public List<Text> description();
    public ItemStack icon();

    public void click(ServerPlayerEntity player, CompletableFuture<ListedGameSpace> future);
    public int getPlayerCount();
}
