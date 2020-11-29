package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.PlaceBlockListener;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity player = context.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(player.world);
        if (gameSpace != null) {
            PlaceBlockListener invoker = gameSpace.invoker(PlaceBlockListener.EVENT);
            ActionResult result = invoker.onPlace(((ServerPlayerEntity) player), context.getBlockPos(), state, context);
            if (result == ActionResult.FAIL) {
                ci.setReturnValue(false);
            }
        }
    }
}
