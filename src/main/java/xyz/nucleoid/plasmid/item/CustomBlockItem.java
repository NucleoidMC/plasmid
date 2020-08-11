package xyz.nucleoid.plasmid.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import xyz.nucleoid.plasmid.block.CustomBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
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
        if (!world.isClient) {
            ItemStack stack = playerEntity.getStackInHand(hand);
            if (CustomItem.match(stack) == item) {
                stack.setCount(stack.getCount() - 1);
                playerEntity.playSound(SoundEvents.BLOCK_STONE_BREAK, 100, 1);
                block.setBlock(blockHitResult.getBlockPos().offset(blockHitResult.getSide().getOpposite().getOpposite()), vanillaState, world);// Based code
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    public CustomItem getItem() {
        return item;
    }

    public CustomBlock getBlock() {
        return block;
    }
}
