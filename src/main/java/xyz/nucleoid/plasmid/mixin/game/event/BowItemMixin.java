package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.PlayerFireArrowListener;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void onStoppedUsing(
            ItemStack tool,
            World world,
            LivingEntity user,
            int remainingUseTicks,
            CallbackInfo ci,
            PlayerEntity playerEntity,
            boolean bl,
            ItemStack itemStack,
            int i,
            float f,
            boolean bl2,
            ArrowItem arrowItem,
            PersistentProjectileEntity projectile
    ) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
        if (gameSpace != null) {
            try {
                ActionResult result = gameSpace
                        .invoker(PlayerFireArrowListener.EVENT)
                        .onFireArrow((ServerPlayerEntity) playerEntity, tool, arrowItem, remainingUseTicks, projectile);

                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }

            } catch (Exception e) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player fire arrow event", e);
                gameSpace.reportError(e, "Firing arrow");
            }
        }
    }
}
