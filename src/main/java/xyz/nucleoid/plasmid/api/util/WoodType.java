package xyz.nucleoid.plasmid.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;

public enum WoodType {
    OAK(Type.REGULAR, Blocks.OAK_SAPLING, Blocks.OAK_LEAVES, Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_DOOR, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.OAK_BUTTON, Blocks.OAK_PRESSURE_PLATE, Items.OAK_BOAT, Items.OAK_CHEST_BOAT),
    SPRUCE(Type.REGULAR, Blocks.SPRUCE_SAPLING, Blocks.SPRUCE_LEAVES, Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.SPRUCE_BUTTON, Blocks.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_BOAT, Items.SPRUCE_CHEST_BOAT),
    BIRCH(Type.REGULAR, Blocks.BIRCH_SAPLING, Blocks.BIRCH_LEAVES, Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_DOOR, Blocks.BIRCH_SIGN,  Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.BIRCH_BUTTON, Blocks.BIRCH_PRESSURE_PLATE, Items.BIRCH_BOAT, Items.BIRCH_CHEST_BOAT),
    JUNGLE(Type.REGULAR, Blocks.JUNGLE_SAPLING, Blocks.JUNGLE_LEAVES, Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_SIGN,  Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.JUNGLE_BUTTON, Blocks.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_BOAT, Items.JUNGLE_CHEST_BOAT),
    ACACIA(Type.REGULAR, Blocks.ACACIA_SAPLING, Blocks.ACACIA_LEAVES, Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_DOOR, Blocks.ACACIA_SIGN,  Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.ACACIA_BUTTON, Blocks.ACACIA_PRESSURE_PLATE, Items.ACACIA_BOAT, Items.ACACIA_CHEST_BOAT),
    DARK_OAK(Type.REGULAR, Blocks.DARK_OAK_SAPLING, Blocks.DARK_OAK_LEAVES, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.DARK_OAK_BUTTON, Blocks.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_BOAT, Items.DARK_OAK_CHEST_BOAT),
    CHERRY(Type.REGULAR, Blocks.CHERRY_SAPLING, Blocks.CHERRY_LEAVES, Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, Blocks.CHERRY_STAIRS, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE, Blocks.CHERRY_DOOR, Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.CHERRY_BUTTON, Blocks.CHERRY_PRESSURE_PLATE, Items.CHERRY_BOAT, Items.CHERRY_CHEST_BOAT),
    MANGROVE(Type.REGULAR, Blocks.MANGROVE_PROPAGULE, Blocks.MANGROVE_LEAVES, Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_SIGN,  Blocks.MANGROVE_WALL_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.MANGROVE_BUTTON, Blocks.MANGROVE_PRESSURE_PLATE, Items.MANGROVE_BOAT, Items.MANGROVE_CHEST_BOAT),
    PALE_OAK(Type.REGULAR, Blocks.PALE_OAK_SAPLING, Blocks.PALE_OAK_LEAVES, Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_FENCE_GATE, Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_SIGN,  Blocks.PALE_OAK_WALL_SIGN, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_BUTTON, Blocks.PALE_OAK_PRESSURE_PLATE, Items.PALE_OAK_BOAT, Items.PALE_OAK_CHEST_BOAT),
    // Put here non-full wood-like types (aka with fallback blocks/items/etc.)
    CRIMSON(Type.NETHER, Blocks.CRIMSON_FUNGUS, Blocks.NETHER_WART_BLOCK, Blocks.CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_SIGN,  Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_PRESSURE_PLATE, Items.MANGROVE_BOAT, Items.MANGROVE_CHEST_BOAT),
    WARPED(Type.NETHER, Blocks.WARPED_FUNGUS, Blocks.WARPED_WART_BLOCK, Blocks.WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_DOOR, Blocks.WARPED_SIGN,  Blocks.WARPED_WALL_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.WARPED_BUTTON, Blocks.WARPED_PRESSURE_PLATE, Items.BIRCH_BOAT, Items.BIRCH_CHEST_BOAT),

    BAMBOO(Type.BAMBOO, Blocks.BAMBOO, Blocks.JUNGLE_LEAVES, Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB, Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_FENCE, Blocks.BAMBOO_FENCE_GATE, Blocks.BAMBOO_DOOR, Blocks.BAMBOO_SIGN,  Blocks.BAMBOO_WALL_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN, Blocks.BAMBOO_BUTTON, Blocks.BAMBOO_PRESSURE_PLATE, Items.BAMBOO_RAFT, Items.BAMBOO_CHEST_RAFT);

    private final Block plant, leaves, log, wood, strippedLog, strippedWood, planks, slab, stairs, fence, fenceGate, door, sign, wallSign, hangingSign, wallHangingSign, button, pressurePlate;
    private final Item boat, chestBoat;
    private final Type type;
    private final FeatureSet requiredFeatures;

    WoodType(Type type,  Block plant, Block leaves, Block log, Block wood, Block strippedLog, Block strippedWood, Block planks, Block slab, Block stairs,
             Block fence, Block fenceGate, Block door, Block sign, Block wallSign, Block hangingSign, Block hangingWallSign, Block button, Block pressurePlate, Item boat, Item chestBoat, FeatureFlag... requiredFeatures) {
        this.type = type;
        this.plant = plant;
        this.leaves = leaves;
        this.log = log;
        this.wood = wood;
        this.strippedLog = strippedLog;
        this.strippedWood = strippedWood;
        this.planks = planks;
        this.slab = slab;
        this.stairs = stairs;
        this.fence = fence;
        this.fenceGate = fenceGate;
        this.door = door;
        this.sign = sign;
        this.wallSign = wallSign;
        this.hangingSign = hangingSign;
        this.wallHangingSign = hangingWallSign;
        this.button = button;
        this.pressurePlate = pressurePlate;
        this.boat = boat;
        this.chestBoat = chestBoat;

        this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(requiredFeatures);
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
        return block == this.plant || (this.type.hasLeavesLike() && block == this.leaves) || block == this.log  || block == this.strippedLog || (this.type.hasWood && (block == this.wood || block == this.strippedWood)) || block == this.planks || block == this.slab || block == this.stairs || block == this.fence || block == this.fenceGate || block == this.door || block == this.sign || block == this.button || block == this.pressurePlate;
    }

    public boolean contains(Item item) {
        if(item instanceof BlockItem blockItem) {
            return this.contains(blockItem.getBlock());
        }
        else {
            return this.type.hasBoats() && (item == this.boat || item == this.chestBoat);
        }
    }

    public boolean isEnabled(FeatureSet enabledFeatures) {
        return this.getRequiredFeatures().isSubsetOf(enabledFeatures);
    }

    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    public Type type() {
        return this.type;
    }

    @Deprecated
    public Block getSapling() {
        return this.plant;
    }

    public Block getPlant() {
        return this.plant;
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
    public Block getWallSign() {
        return this.wallSign;
    }

    public Block getHangingSign() {
        return this.hangingSign;
    }
    public Block getWallHangingSign() {
        return this.wallHangingSign;
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

    public enum Type {
        REGULAR(true, true, true, true),
        NETHER(false, true, true, false),
        BAMBOO(true, false, false, false);

        final boolean hasBoats, hasWood, hasLeavesLike, hasLeaves;

        Type(boolean hasBoats, boolean hasWood, boolean hasLeaveLike, boolean hasLeaves) {
            this.hasBoats = hasBoats;
            this.hasWood = hasWood;
            this.hasLeavesLike = hasLeaveLike;
            this.hasLeaves = hasLeaves;
        }

        public boolean hasBoats() {
            return this.hasBoats;
        }

        public boolean hasWood() {
            return this.hasWood;
        }

        public boolean hasLeavesLike() {
            return this.hasLeavesLike;
        }

        public boolean hasLeaves() {
            return this.hasLeaves;
        }
    }
}
