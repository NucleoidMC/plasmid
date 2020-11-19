package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.BlockHitListener;
import xyz.nucleoid.plasmid.game.event.EntityHitListener;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
    private void onCollision(HitResult hitResult, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);

        if (gameSpace != null) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                ActionResult result = gameSpace.invoker(EntityHitListener.EVENT).onEntityHit((EntityHitResult) hitResult);

                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                ActionResult result = gameSpace.invoker(BlockHitListener.EVENT).onBlockHit((BlockHitResult) hitResult);

                if (result == ActionResult.FAIL) {
                    ci.cancel();
                }
            }
        }
    }
}
