package xyz.nucleoid.plasmid.shop;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.function.Consumer;

public final class ShopEntry {
    private final ItemStackBuilder icon;
    private Cost cost = Cost.no();
    private Consumer<ServerPlayerEntity> buyAction;

    private ShopEntry(ItemStack icon) {
        this.icon = ItemStackBuilder.of(icon);
    }

    public static ShopEntry ofIcon(ItemStack icon) {
        return new ShopEntry(icon);
    }

    public static ShopEntry ofIcon(ItemConvertible icon) {
        return new ShopEntry(new ItemStack(icon));
    }

    public static ShopEntry buyItem(ItemStack stack) {
        ItemStack icon = stack.copy();

        MutableText count = new LiteralText(stack.getCount() + "x ");
        Text name = icon.getName().shallowCopy().formatted(Formatting.BOLD);
        icon.setCustomName(count.append(name));

        return new ShopEntry(stack).onBuy(player -> {
            player.inventory.offerOrDrop(player.world, stack);
        });
    }

    public ShopEntry withCost(Cost cost) {
        this.cost = cost;
        return this;
    }

    public ShopEntry withName(Text name) {
        this.icon.setName(name);
        return this;
    }

    public ShopEntry addLore(Text lore) {
        this.icon.addLore(lore);
        return this;
    }

    public ShopEntry onBuy(Consumer<ServerPlayerEntity> action) {
        this.buyAction = action;
        return this;
    }

    ItemStack createIcon(ServerPlayerEntity player) {
        ItemStack icon = this.icon.build().copy();

        boolean canBuy = this.cost.tryTake(player, true);

        Style style = Style.EMPTY.withItalic(false).withColor(canBuy ? Formatting.BLUE : Formatting.RED);

        Text costText = this.cost.getDisplay();
        costText = new LiteralText(" (").append(costText).append(")").setStyle(costText.getStyle());

        Text name = icon.getName().shallowCopy().setStyle(style).append(costText);
        icon.setCustomName(name);

        return icon;
    }

    void onClick(ServerPlayerEntity player) {
        SoundEvent sound;
        if (this.cost.tryTake(player, false)) {
            if (this.buyAction != null) {
                this.buyAction.accept(player);
            }
            sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        } else {
            sound = SoundEvents.ENTITY_VILLAGER_NO;
        }

        player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
    }
}
