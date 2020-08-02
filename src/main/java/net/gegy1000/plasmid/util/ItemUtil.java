package net.gegy1000.plasmid.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ItemUtil {
    public static ItemStack unbreakable(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        return stack;
    }

    public static ItemStack createFirework(int color, int flight, FireworkItem.Type type) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

        ItemStack star = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag explosion = star.getOrCreateSubTag("Explosion");

        explosion.putIntArray("Colors", new int[] { color });
        explosion.putByte("Type", (byte) type.getId());

        CompoundTag fireworks = rocket.getOrCreateSubTag("Fireworks");

        ListTag explosions = new ListTag();
        explosions.add(explosion);
        fireworks.put("Explosions", explosions);

        fireworks.putByte("Flight", (byte) flight);

        return rocket;
    }

    public static ItemStack dye(ItemStack stack, int color) {
        Item item = stack.getItem();
        if (item instanceof DyeableItem) {
            ((DyeableItem) item).setColor(stack, color);
        }
        return stack;
    }

    public static int getEnchantLevel(ItemStack stack, Enchantment enchantment) {
        Identifier id = Registry.ENCHANTMENT.getId(enchantment);
        if (id == null) return 0;

        String idString = id.toString();

        ListTag enchantmentList = stack.getEnchantments();
        for (int i = 0; i < enchantmentList.size(); i++) {
            CompoundTag enchantmentTag = enchantmentList.getCompound(i);
            if (enchantmentTag.getString("id").equals(idString)) {
                return enchantmentTag.getShort("lvl");
            }
        }

        return 0;
    }

    public static void removeEnchant(ItemStack stack, Enchantment enchantment) {
        Identifier id = Registry.ENCHANTMENT.getId(enchantment);
        if (id == null) return;

        String idString = id.toString();
        ListTag enchantmentList = stack.getEnchantments();
        for (int i = 0; i < enchantmentList.size(); i++) {
            CompoundTag enchantmentTag = enchantmentList.getCompound(i);
            if (enchantmentTag.getString("id").equals(idString)) {
                enchantmentList.remove(i);
                return;
            }
        }
    }
}
