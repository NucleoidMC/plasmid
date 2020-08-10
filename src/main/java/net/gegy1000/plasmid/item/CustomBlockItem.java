package net.gegy1000.plasmid.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.block.CustomBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomBlockItem {

    private final CustomItem item;
    private final CustomBlock block;
    private final BlockState vanillaState;

    public CustomBlockItem(Text name, Identifier identifier, BlockState vanillaState) {
        this.item = CustomItem.builder()
                .id(identifier)
                .name(name)
                .register();
        this.block = CustomBlock.builder()
                .id(identifier)
                .name(name)
                .register();
        this.vanillaState = vanillaState;
        UseBlockCallback.EVENT.register(this::onUse);
    }

    private ActionResult onUse(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
        if(!world.isClient){
            ItemStack stack = playerEntity.getStackInHand(hand);
            CompoundTag tag = stack.getOrCreateTag();
            if(tag.getString(Plasmid.ID + ":custom_item").equals(item.getIdentifier().toString())){
                stack.setCount(stack.getCount()-1);
                playerEntity.playSound(SoundEvents.BLOCK_STONE_BREAK, 100, 1);
                block.setBlock(calcBlockPos(blockHitResult), vanillaState, world);
            }
        }
        return ActionResult.PASS;
    }

    private BlockPos calcBlockPos(BlockHitResult blockHitResult) {
        return blockHitResult.getBlockPos();//TODO: make it use the side it was clicked on to return where a new block should be placed
    }

    public CustomItem getItem(){
        return item;
    }

    public CustomBlock getBlock(){
        return block;
    }
}
