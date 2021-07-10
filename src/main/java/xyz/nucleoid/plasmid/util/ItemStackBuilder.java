package xyz.nucleoid.plasmid.util;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

    public static ItemStackBuilder firework(int color, int flight, FireworkItem.Type type) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

        ItemStack star = new ItemStack(Items.FIREWORK_STAR);
        NbtCompound explosion = star.getOrCreateSubTag("Explosion");

        explosion.putIntArray("Colors", new int[] { color });
        explosion.putByte("Type", (byte) type.getId());

        NbtCompound fireworks = rocket.getOrCreateSubTag("Fireworks");

        NbtList explosions = new NbtList();
        explosions.add(explosion);
        fireworks.put("Explosions", explosions);

        fireworks.putByte("Flight", (byte) flight);

        return new ItemStackBuilder(rocket);
    }

    public ItemStackBuilder setCount(int count) {
        this.stack.setCount(count);
        return this;
    }

    public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
        this.stack.addEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder addCanDestroy(Block block) {
        Identifier blockId = Registry.BLOCK.getId(block);
        return this.addCanDestroy(blockId.toString());
    }

    public ItemStackBuilder addCanDestroy(Tag<Block> block) {
        Identifier tagId = BlockTags.getTagGroup().getUncheckedTagId(block);
        if (tagId == null) {
            throw new IllegalArgumentException("tag " + block + " does not exist!");
        }

        return this.addCanDestroy("#" + tagId.toString());
    }

    public ItemStackBuilder addCanPlaceOn(Block block) {
        Identifier blockId = Registry.BLOCK.getId(block);
        return this.addCanPlaceOn(blockId.toString());
    }

    public ItemStackBuilder addCanPlaceOn(Tag<Block> block) {
        Identifier tagId = BlockTags.getTagGroup().getUncheckedTagId(block);
        if (tagId == null) {
            throw new IllegalArgumentException("tag " + block + " does not exist!");
        }

        return this.addCanPlaceOn("#" + tagId.toString());
    }

    private ItemStackBuilder addCanDestroy(String predicate) {
        return this.addPredicate("CanDestroy", predicate);
    }

    private ItemStackBuilder addCanPlaceOn(String predicate) {
        return this.addPredicate("CanPlaceOn", predicate);
    }

    private ItemStackBuilder addPredicate(String key, String predicate) {
        NbtCompound tag = this.stack.getOrCreateTag();

        NbtList predicateList;

        if (tag.contains(key, NbtType.LIST)) {
            predicateList = tag.getList(key, NbtType.STRING);
        } else {
            predicateList = new NbtList();
            tag.put(key, predicateList);
        }

        predicateList.add(NbtString.of(predicate));

        return this;
    }

    public ItemStackBuilder setUnbreakable() {
        NbtCompound tag = this.stack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        return this;
    }

    public ItemStackBuilder setDyeColor(int color) {
        Item item = this.stack.getItem();
        if (item instanceof DyeableItem) {
            ((DyeableItem) item).setColor(this.stack, color);
        }
        return this;
    }

    public ItemStackBuilder setName(Text text) {
        this.stack.setCustomName(text);
        return this;
    }

    public ItemStackBuilder addLore(Text text) {
        NbtCompound display = this.stack.getOrCreateSubTag("display");

        NbtList loreList;
        if (display.contains("Lore", 9)) {
            loreList = display.getList("Lore", 8);
        } else {
            loreList = new NbtList();
            display.put("Lore", loreList);
        }

        loreList.add(NbtString.of(Text.Serializer.toJson(text)));

        return this;
    }

    public ItemStackBuilder addModifier(EntityAttribute attribute, EntityAttributeModifier modifier, EquipmentSlot slot) {
        this.stack.addAttributeModifier(attribute, modifier, slot);
        return this;
    }

    public ItemStackBuilder setRepairCost(int repairCost) {
        this.stack.setRepairCost(repairCost);
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
