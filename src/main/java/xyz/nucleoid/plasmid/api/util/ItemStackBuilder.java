package xyz.nucleoid.plasmid.api.util;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ItemStackBuilder {
    private final ItemStack stack;

    private ItemStackBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemStackBuilder of(ItemConvertible item) {
        return new ItemStackBuilder(new ItemStack(item));
    }

    public static ItemStackBuilder of(ItemStack stack) {
        return new ItemStackBuilder(stack.copy());
    }

    public static ItemStackBuilder firework(int color, int flight, FireworkExplosionComponent.Type type) {
        var rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

        rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(flight, List.of(
                new FireworkExplosionComponent(type, IntList.of(color), IntList.of(), false, false)
        )));

        return new ItemStackBuilder(rocket);
    }

    public ItemStackBuilder setCount(int count) {
        this.stack.setCount(count);
        return this;
    }

    public ItemStackBuilder addEnchantment(MinecraftServer server, RegistryKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(server.getRegistryManager(), enchantment, level);
    }

    public ItemStackBuilder addEnchantment(World world, RegistryKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(world.getRegistryManager(), enchantment, level);
    }

    public ItemStackBuilder addEnchantment(RegistryWrapper.WrapperLookup lookup, RegistryKey<Enchantment> enchantment, int level) {
        return this.addEnchantment(lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    public ItemStackBuilder addEnchantment(RegistryEntry<Enchantment> enchantment, int level) {
        this.stack.addEnchantment(enchantment, level);
        return this;
    }

    public <T> ItemStackBuilder set(ComponentType<T> type, @Nullable T value) {
        this.stack.set(type,value);
        return this;
    }

    public ItemStackBuilder setUnbreakable() {
        this.stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        return this;
    }

    public ItemStackBuilder setDyeColor(int color) {
        this.stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, true));
        return this;
    }

    public ItemStackBuilder setName(Text text) {
        this.stack.set(DataComponentTypes.ITEM_NAME, text);
        return this;
    }

    public ItemStackBuilder addLore(Text text) {
        this.stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, x -> x.with(text));
        return this;
    }

    public ItemStackBuilder addModifier(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
        this.stack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT,
                x -> x.with(attribute, modifier, slot));
        return this;
    }

    public ItemStackBuilder setRepairCost(int repairCost) {
        this.stack.set(DataComponentTypes.REPAIR_COST, repairCost);
        return this;
    }

    public ItemStackBuilder setDamage(int damage) {
        this.stack.setDamage(damage);
        return this;
    }

    public ItemStack build() {
        return this.stack.copy();
    }
}
