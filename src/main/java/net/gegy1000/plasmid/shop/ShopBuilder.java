package net.gegy1000.plasmid.shop;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class ShopBuilder {
    private final ServerPlayerEntity player;
    final List<ShopUi.Element> elements = new ArrayList<>();

    ShopBuilder(ServerPlayerEntity player) {
        this.player = player;
    }

    public ShopBuilder addItem(ItemStack stack, Cost cost) {
        MutableText count = new LiteralText(stack.getCount() + "x ");
        Text name = stack.getName().shallowCopy().formatted(Formatting.BOLD);
        Text text = count.append(name);
        return this.add(stack.getItem(), cost, text, () -> {
            this.player.inventory.offerOrDrop(this.player.world, stack);
        });
    }

    public ShopBuilder add(ItemConvertible icon, Cost cost, Text name, Runnable onBuy) {
        this.elements.add(new ShopUi.Element(new ItemStack(icon), name, cost, onBuy));
        return this;
    }
}
