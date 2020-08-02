package net.gegy1000.plasmid.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                // resend to avoid duplication
                serverPlayer.onHandlerRegistered(this, this.getStacks());
                return ItemStack.EMPTY;
            }
        };
    }

    public static class Element {
        private final ItemStack icon;
        private final Text name;
        private final Cost cost;
        private final Runnable onBuy;

        Element(ItemStack icon, Text name, Cost cost, Runnable onBuy) {
            this.icon = icon;
            this.name = name;
            this.cost = cost;
            this.onBuy = onBuy;
        }

        ItemStack getIcon(ServerPlayerEntity player) {
            ItemStack icon = this.icon.copy();

            boolean canBuy = this.cost.tryTake(player, true);

            Style style = Style.EMPTY.withItalic(false).withColor(canBuy ? Formatting.BLUE : Formatting.RED);

            Text costText = this.cost.getDisplay();
            costText = new LiteralText(" (").append(costText).append(")").setStyle(costText.getStyle());

            Text name = this.name.shallowCopy().setStyle(style).append(costText);
            icon.setCustomName(name);

            return icon;
        }

        void onClick(ServerPlayerEntity player) {
            SoundEvent sound;
            if (this.cost.tryTake(player, false)) {
                this.onBuy.run();
                sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            } else {
                sound = SoundEvents.ENTITY_VILLAGER_NO;
            }

            player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
        }
    }
}
