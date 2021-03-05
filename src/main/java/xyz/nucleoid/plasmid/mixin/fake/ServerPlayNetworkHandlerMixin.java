package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.fake.FakeItem;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    // Code made by TheEpicBlock_TEB from PolyMC.
    @Redirect(method = "onClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean areEqualRedirect(ItemStack left, ItemStack right) {
        return ItemStack.areEqual(left, FakeItem.getProxy(right));
    }
}
