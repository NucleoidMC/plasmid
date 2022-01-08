package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public record WoodType(Block sapling, Block pottedSapling, Block leaves, Block log, Block wood, Block planks, Block slab, Block stairs, Block fence, Block fenceGate, Block door, Block trapdoor, Block sign, Block wallSign, Block button, Block pressurePlate, @Nullable Item boat) {
    public static final WoodType OAK = register(new WoodType(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, Blocks.OAK_LEAVES, Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_DOOR, Blocks.OAK_TRAPDOOR, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.OAK_BUTTON, Blocks.OAK_PRESSURE_PLATE, Items.OAK_BOAT));
    public static final WoodType SPRUCE = register(new WoodType(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, Blocks.SPRUCE_LEAVES, Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_BUTTON, Blocks.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_BOAT));
    public static final WoodType BIRCH = register(new WoodType(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.BIRCH_LEAVES, Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_DOOR, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_BUTTON, Blocks.BIRCH_PRESSURE_PLATE, Items.BIRCH_BOAT));
    public static final WoodType JUNGLE = register(new WoodType(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, Blocks.JUNGLE_LEAVES, Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_BUTTON, Blocks.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_BOAT));
    public static final WoodType ACACIA = register(new WoodType(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, Blocks.ACACIA_LEAVES, Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_DOOR, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_BUTTON, Blocks.ACACIA_PRESSURE_PLATE, Items.ACACIA_BOAT));
    public static final WoodType DARK_OAK = register(new WoodType(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, Blocks.DARK_OAK_LEAVES, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_BUTTON, Blocks.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_BOAT));
    public static final WoodType CRIMSON = register(new WoodType(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, Blocks.NETHER_WART_BLOCK, Blocks.CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_PRESSURE_PLATE, null));
    public static final WoodType WARPED = register(new WoodType(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, Blocks.WARPED_WART_BLOCK, Blocks.WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_DOOR, Blocks.WARPED_TRAPDOOR, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.WARPED_BUTTON, Blocks.WARPED_PRESSURE_PLATE, null));

    private static final Set<WoodType> TYPES = new HashSet<>();
    
    private static WoodType register(WoodType woodType) {
        TYPES.add(woodType);
        return woodType;
    }
    
    public static Set<WoodType> values() {
        return TYPES;
    }

    public static WoodType getType(Block block) {
        for (var type : TYPES) {
            if(type.contains(block)) {
                return type;
            }
        }
        return null;
    }

    public static WoodType getType(Item item) {
        for (var type : TYPES) {
            if(type.contains(item)) {
                return type;
            }
        }
        return null;
    }

    public boolean contains(Block block) {
        return block == this.sapling || block == this.pottedSapling || block == this.leaves || block == this.log || block == this.wood || block == this.planks || block == this.slab || block == this.stairs || block == this.fence || block == this.fenceGate || block == this.door || block == this.trapdoor || block == this.sign || block == this.wallSign || block == this.button || block == this.pressurePlate;
    }

    public boolean contains(Item item) {
        if(item instanceof BlockItem blockItem) {
            return this.contains(blockItem.getBlock());
        } else {
            return item == this.boat;
        }
    }

    /**
     * @deprecated Use {@link #sapling()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getSapling() {
        return this.sapling;
    }

    /**
     * @deprecated Use {@link #leaves()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getLeaves() {
        return this.leaves;
    }

    /**
     * @deprecated Use {@link #log()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getLog() {
        return this.log;
    }

    /**
     * @deprecated Use {@link #planks()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getPlanks() {
        return this.planks;
    }

    /**
     * @deprecated Use {@link #slab()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getSlab() {
        return this.slab;
    }

    /**
     * @deprecated Use {@link #stairs()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getStairs() {
        return this.stairs;
    }

    /**
     * @deprecated Use {@link #fence()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getFence() {
        return this.fence;
    }

    /**
     * @deprecated Use {@link #fenceGate()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getFenceGate() {
        return this.fenceGate;
    }

    /**
     * @deprecated Use {@link #door()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getDoor() {
        return this.door;
    }

    /**
     * @deprecated Use {@link #sign()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getSign() {
        return this.sign;
    }

    /**
     * @deprecated Use {@link #button()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getButton() {
        return this.button;
    }

    /**
     * @deprecated Use {@link #pressurePlate()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Block getPressurePlate() {
        return this.pressurePlate;
    }

    /**
     * @deprecated Use {@link #boat()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Nullable
    public Item getBoat() {
        return this.boat;
    }
}
