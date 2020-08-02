package net.gegy1000.plasmid.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class ShopUi implements NamedScreenHandlerFactory {
    private final Text title;
    private final Consumer<ShopBuilder> builder;

    ShopUi(Text title, Consumer<ShopBuilder> builder) {
        this.title = title;
        this.builder = builder;
    }

    public static ShopUi create(Text title, Consumer<ShopBuilder> builder) {
        return new ShopUi(title, builder);
    }

    @Override
    public Text getDisplayName() {
        return this.title;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        ShopInventory inventory = new ShopInventory(serverPlayer, this.builder);
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, inventory, 5) {
            @Override
            public ItemStack transferSlot(PlayerEntity player, int invSlot) {
                // resend inventory to avoid duplication
                serverPlayer.onHandlerRegistered(this, this.getStacks());
                return ItemStack.EMPTY;
            }
        };
    }
}
