package xyz.nucleoid.plasmid.shop;

import eu.pb4.sgui.api.elements.GuiElement;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.function.Consumer;

public final class ShopEntry {
    private final ItemStackBuilder icon;
    private Cost cost;
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
        var icon = stack.copy();

        var count = new LiteralText(stack.getCount() + "x ");
        var name = icon.getName().shallowCopy().formatted(Formatting.BOLD);
        icon.setCustomName(count.append(name));

        return new ShopEntry(icon).onBuy(player -> {
            player.getInventory().offerOrDrop(stack);
        });
    }

    public ShopEntry withCost(Cost cost) {
        this.cost = cost;
        return this;
    }

    public ShopEntry noCost() {
        this.cost = null;
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
        var icon = this.icon.build();

        if (this.cost != null) {
            boolean canBuy = this.cost.tryTake(player, true);

            var style = Style.EMPTY.withItalic(false).withColor(canBuy ? Formatting.BLUE : Formatting.RED);

            var costText = this.cost.getDisplay();
            costText = new LiteralText(" (").append(costText).append(")").setStyle(costText.getStyle());

            var name = icon.getName().shallowCopy().setStyle(style).append(costText);
            icon.setCustomName(name);
        }

        return icon;
    }

    void onClick(ServerPlayerEntity player) {
        SoundEvent sound;
        if (this.cost == null || this.cost.tryTake(player, false)) {
            if (this.buyAction != null) {
                this.buyAction.accept(player);
            }
            sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        } else {
            sound = SoundEvents.ENTITY_VILLAGER_NO;
        }

        player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
    }

    public GuiElement createGuiElement(ServerPlayerEntity player) {
        var icon = this.createIcon(player);
        return new GuiElement(icon, (index, type, action) -> {
            this.onClick(player);
        });
    }
}
