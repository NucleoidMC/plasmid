package xyz.nucleoid.plasmid.util;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
        Identifier tagId = BlockTags.getTagGroup().getTagId(block);
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
        Identifier tagId = BlockTags.getTagGroup().getTagId(block);
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
        CompoundTag tag = this.stack.getOrCreateTag();

        ListTag predicateList;

        if (tag.contains(key, NbtType.LIST)) {
            predicateList = tag.getList(key, NbtType.STRING);
        } else {
            predicateList = new ListTag();
            tag.put(key, predicateList);
        }

        predicateList.add(StringTag.of(predicate));

        return this;
    }

    public ItemStackBuilder setUnbreakable() {
        CompoundTag tag = this.stack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        return this;
    }

    public ItemStackBuilder setColor(int color) {
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
        CompoundTag display = this.stack.getOrCreateSubTag("display");

        ListTag loreList;
        if (display.contains("Lore", 9)) {
            loreList = display.getList("Lore", 8);
        } else {
            loreList = new ListTag();
            display.put("Lore", loreList);
        }

        loreList.add(StringTag.of(Text.Serializer.toJson(text)));

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
