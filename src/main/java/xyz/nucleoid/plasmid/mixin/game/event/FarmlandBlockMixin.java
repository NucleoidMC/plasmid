package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Inject(method = "onLandedUpon", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void breakFarmland(World world, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) {
            ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
            if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) entity)) {
                try {
                    BreakBlockListener invoker = gameSpace.invoker(BreakBlockListener.EVENT);
                    if (invoker.onBreak((ServerPlayerEntity) entity, pos) == ActionResult.FAIL) {
                        ci.cancel();
                    }
                } catch (Exception e) {
                    Plasmid.LOGGER.error("An unexpected exception occurred while dispatching block break event", e);
                }
            }
        }
    }
}
