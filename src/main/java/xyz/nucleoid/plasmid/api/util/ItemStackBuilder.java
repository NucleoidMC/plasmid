package xyz.nucleoid.plasmid.api.util;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ItemStackBuilder {
    private final ItemStack stack;

    private ItemStackBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemStackBuilder of(ItemLike item) {
        return new ItemStackBuilder(new ItemStack(item));
    }

    public static ItemStackBuilder of(ItemStack stack) {
        return new ItemStackBuilder(stack.copy());
    }

    public static ItemStackBuilder firework(int color, int flight, FireworkExplosion.Shape type) {
        var rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

        rocket.set(DataComponents.FIREWORKS, new Fireworks(flight, List.of(
                new FireworkExplosion(type, IntList.of(color), IntList.of(), false, false)
        )));

        return new ItemStackBuilder(rocket);
    }

    public ItemStackBuilder setCount(int count) {
        this.stack.setCount(count);
        return this;
    }

    public ItemStackBuilder addEnchantment(MinecraftServer server, ResourceKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(server.registryAccess(), enchantment, level);
    }

    public ItemStackBuilder addEnchantment(Level world, ResourceKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(world.registryAccess(), enchantment, level);
    }

    public ItemStackBuilder addEnchantment(HolderLookup.Provider lookup, ResourceKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    public ItemStackBuilder addEnchantment(Holder<Enchantment> enchantment, int level) {
        this.stack.enchant(enchantment, level);
        return this;
    }

    public <T> ItemStackBuilder set(DataComponentType<T> type, @Nullable T value) {
        this.stack.set(type,value);
        return this;
    }

    public ItemStackBuilder setUnbreakable() {
        this.stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        this.stack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, tooltipDisplay -> tooltipDisplay.withHidden(DataComponents.UNBREAKABLE, true));

        return this;
    }

    public ItemStackBuilder setDyeColor(int color) {
        this.stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        this.stack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, tooltipDisplay -> tooltipDisplay.withHidden(DataComponents.DYED_COLOR, true));

        return this;
    }

    public ItemStackBuilder setName(Component text) {
        this.stack.set(DataComponents.ITEM_NAME, text);
        return this;
    }

    public ItemStackBuilder addLore(Component text) {
        this.stack.update(DataComponents.LORE, ItemLore.EMPTY, x -> x.withLineAdded(text));
        return this;
    }

    public ItemStackBuilder addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        this.stack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY,
                x -> x.withModifierAdded(attribute, modifier, slot));
        return this;
    }

    public ItemStackBuilder setRepairCost(int repairCost) {
        this.stack.set(DataComponents.REPAIR_COST, repairCost);
        return this;
    }

    public ItemStackBuilder setDamage(int damage) {
        this.stack.setDamageValue(damage);
        return this;
    }

    public ItemStack build() {
        return this.stack.copy();
    }
}
