package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;

public interface MenuEntry {
    public Text name();
    public List<Text> description();
    public ItemStack icon();

    public void click(ServerPlayerEntity player, CompletableFuture<GameSpace> future);
    public int getPlayerCount();
}