package xyz.nucleoid.plasmid.api.shop;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.GuiLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.api.util.PlayerUtil;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@SuppressWarnings({"unused"})
public final class ShopEntry implements GuiElement {
    private final ItemStackBuilder defaultIcon;
    @Nullable
    private BiFunction<ServerPlayer, ShopEntry, @NotNull Cost> cost;

    private BiFunction<ServerPlayer, ShopEntry, ItemStack> icon;
    private BiPredicate<ServerPlayer, ShopEntry> canBuy;
    private BiPredicate<ServerPlayer, ShopEntry> preBuyCheck = (serverPlayerEntity, entry) -> true;
    private Consumer<ServerPlayer> buyAction;

    private ShopEntry(ItemStack defaultIcon) {
        this.defaultIcon = ItemStackBuilder.of(defaultIcon);
        this.icon = this::defaultIconBuilder;
    }

    private ItemStack defaultIconBuilder(ServerPlayer player, ShopEntry entry) {
        var icon = this.defaultIcon.build();

        boolean canBuy = this.canBuy.test(player, entry);

        var style = Style.EMPTY.withItalic(false).withColor(canBuy ? ChatFormatting.BLUE : ChatFormatting.RED);
        var name = icon.getHoverName().copy().setStyle(style);

        if (this.cost != null) {
            var cost = this.cost.apply(player, entry);
            var costText = cost.getDisplay();
            costText = Component.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        icon.set(DataComponents.CUSTOM_NAME, name);

        return icon;
    }

    public static ShopEntry ofIcon(ItemStack icon) {
        return new ShopEntry(icon);
    }

    public static ShopEntry ofIcon(ItemLike icon) {
        return new ShopEntry(new ItemStack(icon));
    }

    public static ShopEntry ofIcon(BiFunction<ServerPlayer, ShopEntry, ItemStack> iconBuilder) {
        var entry = new ShopEntry(ItemStack.EMPTY);
        entry.icon = iconBuilder;
        return entry;
    }

    public static ShopEntry buyItem(ItemStack stack) {
        var icon = stack.copy();

        var count = Component.literal(stack.getCount() + "x ");
        var name = icon.getHoverName().copy().withStyle(ChatFormatting.BOLD);
        icon.set(DataComponents.CUSTOM_NAME, count.append(name).withStyle(SguiUtils.STYLE_CLEARER));

        return new ShopEntry(icon).onBuy((player) -> player.getInventory().placeItemBackInInventory(stack.copy()));
    }

    public static ShopEntry buyItem(ItemStack stack, Cost cost) {
        var icon = stack.copy();

        var count = Component.literal(stack.getCount() + "x ");
        var name = icon.getHoverName().copy().withStyle(ChatFormatting.BOLD);
        icon.set(DataComponents.CUSTOM_NAME, count.append(name).withStyle(SguiUtils.STYLE_CLEARER));

        return new ShopEntry(icon).onBuy((player) -> player.getInventory().placeItemBackInInventory(stack.copy())).withCost(cost);
    }

    public ShopEntry onBuy(Consumer<ServerPlayer> action) {
        this.buyAction = action;
        return this;
    }

    public ShopEntry onBuyCheck(BiPredicate<ServerPlayer, ShopEntry> buyCheck) {
        this.preBuyCheck = buyCheck;
        return this;
    }

    public ShopEntry withCost(Cost cost) {
        this.canBuy = (player, entry) -> cost.canBuy(player);
        this.preBuyCheck = (player, entry) -> cost.takeItems(player);
        this.cost = (player, entry) -> cost;
        return this;
    }

    public ShopEntry withCost(BiFunction<ServerPlayer, ShopEntry, Cost> cost) {
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

    public ShopEntry withName(Component name) {
        this.defaultIcon.setName(name);
        return this;
    }

    public ShopEntry addLore(Component lore) {
        this.defaultIcon.addLore(lore);
        return this;
    }

    @Override
    public ItemStack getItemStack() {
        return this.defaultIcon.build();
    }

    @Nullable
    public Cost getCost(ServerPlayer player) {
        return this.cost != null ? this.cost.apply(player, this) : null;
    }

    public boolean canBuy(ServerPlayer player) {
        return this.canBuy.test(player, this);
    }

    public boolean runPreBuyCheck(ServerPlayer player) {
        return this.preBuyCheck.test(player, this);
    }

    public void runBuyAction(ServerPlayer player) {
        this.buyAction.accept(player);
    }

    @Override
    @ApiStatus.Internal
    public ItemStack getItemStackForDisplay(GuiLike gui) {
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
                sound = SoundEvents.EXPERIENCE_ORB_PICKUP;
            } else {
                sound = SoundEvents.VILLAGER_NO;
            }

            PlayerUtil.playSoundToPlayer(gui.getPlayer(), sound, SoundSource.UI, 1.0F, 1.0F);
        };
    }
}
