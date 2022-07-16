package xyz.nucleoid.plasmid.shop;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.function.*;

@SuppressWarnings({"unused"})
public final class ShopEntry implements GuiElementInterface {
    private final ItemStackBuilder defaultIcon;
    @Nullable
    private BiFunction<ServerPlayerEntity, ShopEntry, @NotNull Cost> cost;

    private BiFunction<ServerPlayerEntity, ShopEntry, ItemStack> icon;
    private BiPredicate<ServerPlayerEntity, ShopEntry> canBuy;
    private BiPredicate<ServerPlayerEntity, ShopEntry> preBuyCheck = (serverPlayerEntity, entry) -> true;
    private Consumer<ServerPlayerEntity> buyAction;

    private ShopEntry(ItemStack defaultIcon) {
        this.defaultIcon = ItemStackBuilder.of(defaultIcon);
        this.icon = this::defaultIconBuilder;
    }

    private ItemStack defaultIconBuilder(ServerPlayerEntity player, ShopEntry entry) {
        var icon = this.defaultIcon.build();

        boolean canBuy = this.canBuy.test(player, entry);

        var style = Style.EMPTY.withItalic(false).withColor(canBuy ? Formatting.BLUE : Formatting.RED);
        var name = icon.getName().copy().setStyle(style);

        if (this.cost != null) {
            var cost = this.cost.apply(player, entry);
            var costText = cost.getDisplay();
            costText = Text.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        icon.setCustomName(name);

        return icon;
    }

    public static ShopEntry ofIcon(ItemStack icon) {
        return new ShopEntry(icon);
    }

    public static ShopEntry ofIcon(ItemConvertible icon) {
        return new ShopEntry(new ItemStack(icon));
    }

    public static ShopEntry ofIcon(BiFunction<ServerPlayerEntity, ShopEntry, ItemStack> iconBuilder) {
        var entry = new ShopEntry(ItemStack.EMPTY);
        entry.icon = iconBuilder;
        return entry;
    }

    public static ShopEntry buyItem(ItemStack stack) {
        var icon = stack.copy();

        var count = Text.literal(stack.getCount() + "x ");
        var name = icon.getName().copy().formatted(Formatting.BOLD);
        icon.setCustomName(count.append(name));

        return new ShopEntry(icon).onBuy((player) -> player.getInventory().offerOrDrop(stack.copy()));
    }

    public static ShopEntry buyItem(ItemStack stack, Cost cost) {
        var icon = stack.copy();

        var count = Text.literal(stack.getCount() + "x ");
        var name = icon.getName().copy().formatted(Formatting.BOLD);
        icon.setCustomName(count.append(name));

        return new ShopEntry(icon).onBuy((player) -> player.getInventory().offerOrDrop(stack.copy())).withCost(cost);
    }

    public ShopEntry onBuy(Consumer<ServerPlayerEntity> action) {
        this.buyAction = action;
        return this;
    }

    public ShopEntry onBuyCheck(BiPredicate<ServerPlayerEntity, ShopEntry> buyCheck) {
        this.preBuyCheck = buyCheck;
        return this;
    }

    public ShopEntry withCost(Cost cost) {
        this.canBuy = (player, entry) -> cost.canBuy(player);
        this.preBuyCheck = (player, entry) -> cost.takeItems(player);
        this.cost = (player, entry) -> cost;
        return this;
    }

    public ShopEntry withCost(BiFunction<ServerPlayerEntity, ShopEntry, Cost> cost) {
        this.canBuy = (player, entry) -> cost.apply(player, entry).canBuy(player);
        this.preBuyCheck = (player, entry) -> cost.apply(player, entry).takeItems(player);
        this.cost = cost;
        return this;
    }

    public ShopEntry noCost() {
        this.canBuy = (player, entry) -> true;
        this.preBuyCheck = (player, entry) -> true;
        this.cost = null;
        return this;
    }

    public ShopEntry withName(Text name) {
        this.defaultIcon.setName(name);
        return this;
    }

    public ShopEntry addLore(Text lore) {
        this.defaultIcon.addLore(lore);
        return this;
    }

    @Override
    public ItemStack getItemStack() {
        return this.defaultIcon.build();
    }

    @Nullable
    public Cost getCost(ServerPlayerEntity player) {
        return this.cost != null ? this.cost.apply(player, this) : null;
    }

    public boolean canBuy(ServerPlayerEntity player) {
        return this.canBuy.test(player, this);
    }

    public boolean runPreBuyCheck(ServerPlayerEntity player) {
        return this.preBuyCheck.test(player, this);
    }

    public void runBuyAction(ServerPlayerEntity player) {
        this.buyAction.accept(player);
    }

    @Override
    @ApiStatus.Internal
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return this.icon.apply(gui.getPlayer(), this);
    }

    @Override
    @ApiStatus.Internal
    public ClickCallback getGuiCallback() {
        return (x, y, z, gui) -> {
            SoundEvent sound;
            if (this.preBuyCheck.test(gui.getPlayer(), this)) {
                if (this.buyAction != null) {
                    this.buyAction.accept(gui.getPlayer());
                }
                sound = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            } else {
                sound = SoundEvents.ENTITY_VILLAGER_NO;
            }

            gui.getPlayer().playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
        };
    }
}
