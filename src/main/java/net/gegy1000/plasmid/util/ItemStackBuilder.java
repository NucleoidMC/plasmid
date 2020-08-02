package net.gegy1000.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
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
        return new ItemStackBuilder(stack);
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
        return this.setCanDestroy(blockId.toString());
    }

    public ItemStackBuilder addCanDestroy(Tag<Block> block) {
        Identifier tagId = BlockTags.getContainer().getId(block);
        if (tagId == null) {
            throw new IllegalArgumentException("tag " + block + " does not exist!");
        }

        return this.setCanDestroy("#" + tagId.toString());
    }

    private ItemStackBuilder setCanDestroy(String predicate) {
        CompoundTag tag = this.stack.getOrCreateTag();

        ListTag canDestroy;

        if (tag.contains("CanDestroy", 9)) {
            canDestroy = tag.getList("CanDestroy", 8);
        } else {
            canDestroy = new ListTag();
            tag.put("CanDestroy", canDestroy);
        }

        canDestroy.add(StringTag.of(predicate));

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

    public ItemStack build() {
        return this.stack;
    }
}
