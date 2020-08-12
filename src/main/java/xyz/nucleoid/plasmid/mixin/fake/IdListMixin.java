package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.fake.Fake;
import xyz.nucleoid.plasmid.fake.FakeBlock;
import xyz.nucleoid.plasmid.fake.FakeItem;

@Mixin(IdList.class)
public class IdListMixin<T> {

    @ModifyVariable(method = "getRawId", at = @At("HEAD"), argsOnly = true, index = 1)
    private T modify(T entry) {
        if (entry == null) {
            return null;
        }

        if (entry instanceof BlockState) {
            BlockState state = (BlockState) entry;
            Block block = state.getBlock();

            if (block instanceof FakeBlock) {
                return (T) ((FakeBlock<?>) block).getFaking(state);
            }
        }

        if (entry instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) entry;
            Item item = itemStack.getItem();

            if (item instanceof FakeItem) {
                return (T) ((FakeItem<?>) item).getFaking(itemStack);
            }
        }

        if (entry instanceof Fake) {
            return (T) ((Fake<?>) entry).getFaking();
        }

        return entry;
    }
}
