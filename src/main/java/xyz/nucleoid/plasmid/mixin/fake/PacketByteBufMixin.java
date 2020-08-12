package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.fake.FakeItem;

@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {

    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), argsOnly = true, index = 1)
    private ItemStack modify(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof FakeItem) {
            return ((FakeItem<?>) item).getFaking(stack);
        } else {
            return stack;
        }
    }
}
