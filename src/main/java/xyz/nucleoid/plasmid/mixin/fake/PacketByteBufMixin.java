package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.fake.Fake;

@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {
    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), index = 1, argsOnly = true)
    private ItemStack modifyItemStack(ItemStack itemStack) {
        return Fake.getProxy(itemStack);
    }

    @Redirect(
            method = "writeItemStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"
            )
    )
    private Item modifyItem(ItemStack stack) {
        return Fake.getProxy(stack.getItem());
    }
}
