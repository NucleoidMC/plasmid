package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum WoodType {
    OAK(Blocks.OAK_SAPLING, Blocks.OAK_LEAVES, Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_DOOR, Blocks.OAK_SIGN, Blocks.OAK_BUTTON, Blocks.OAK_PRESSURE_PLATE, Items.OAK_BOAT),
    SPRUCE(Blocks.SPRUCE_SAPLING, Blocks.SPRUCE_LEAVES, Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_BUTTON, Blocks.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_BOAT),
    BIRCH(Blocks.BIRCH_SAPLING, Blocks.BIRCH_LEAVES, Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_DOOR, Blocks.BIRCH_SIGN, Blocks.BIRCH_BUTTON, Blocks.BIRCH_PRESSURE_PLATE, Items.BIRCH_BOAT),
    JUNGLE(Blocks.JUNGLE_SAPLING, Blocks.JUNGLE_LEAVES, Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_BUTTON, Blocks.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_BOAT),
    ACACIA(Blocks.ACACIA_SAPLING, Blocks.ACACIA_LEAVES, Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_DOOR, Blocks.ACACIA_SIGN, Blocks.ACACIA_BUTTON, Blocks.ACACIA_PRESSURE_PLATE, Items.ACACIA_BOAT),
    DARK_OAK(Blocks.DARK_OAK_SAPLING, Blocks.DARK_OAK_LEAVES, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_BUTTON, Blocks.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_BOAT);
    MANGROVE(Blocks.MANGROVE_PROPAGULE, Blocks.MANGROVE_LEAVES, Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_BUTTON, Blocks.MANGROVE_PRESSURE_PLATE, Items.MANGROVE_BOAT);

    private final Block sapling, leaves, log, planks, slab, stairs, fence, fenceGate, door, sign, button, pressurePlate;
    private final Item boat;

    WoodType(Block sapling, Block leaves, Block log, Block planks, Block slab, Block stairs, Block fence, Block fenceGate, Block door, Block sign, Block button, Block pressurePlate, Item boat) {
        this.sapling = sapling;
        this.leaves = leaves;
        this.log = log;
        this.planks = planks;
        this.slab = slab;
        this.stairs = stairs;
        this.fence = fence;
        this.fenceGate = fenceGate;
        this.door = door;
        this.sign = sign;
        this.button = button;
        this.pressurePlate = pressurePlate;
        this.boat = boat;
    }

    public static WoodType getType(Block block) {
        for (var type : WoodType.values()) {
            if(type.contains(block)) {
                return type;
            }
        }
        return null;
    }

    public static WoodType getType(Item item) {
        for (var type : WoodType.values()) {
            if(type.contains(item)) {
                return type;
            }
        }
        return null;
    }

    public boolean contains(Block block) {
        return block == this.sapling || block == this.leaves || block == this.log || block == this.planks || block == this.slab || block == this.stairs || block == this.fence || block == this.fenceGate || block == this.door || block == this.sign || block == this.button || block == this.pressurePlate;
    }

    public boolean contains(Item item) {
        if(item instanceof BlockItem blockItem) {
            return this.contains(blockItem.getBlock());
        }
        else {
            return item == this.boat;
        }
    }

    public Block getSapling() {
        return this.sapling;
    }

    public Block getLeaves() {
        return this.leaves;
    }

    public Block getLog() {
        return this.log;
    }

    public Block getPlanks() {
        return this.planks;
    }

    public Block getSlab() {
        return this.slab;
    }

    public Block getStairs() {
        return this.stairs;
    }

    public Block getFence() {
        return this.fence;
    }

    public Block getFenceGate() {
        return this.fenceGate;
    }

    public Block getDoor() {
        return this.door;
    }

    public Block getSign() {
        return this.sign;
    }

    public Block getButton() {
        return this.button;
    }

    public Block getPressurePlate() {
        return this.pressurePlate;
    }

    public Item getBoat() {
        return this.boat;
    }
}
