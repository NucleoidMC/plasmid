package xyz.nucleoid.plasmid.mixin.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.EntityDeathListener;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void callDeathListener(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.world.isClient) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(entity.world);

        // validate world & only trigger if this entity is inside it
        if (gameWorld != null && gameWorld.containsEntity(entity)) {
            ActionResult result = gameWorld.invoker(EntityDeathListener.EVENT).onDeath(entity, source);

            // cancel death if FAIL was returned from any listener
            if (result == ActionResult.FAIL) {
                ci.cancel();
            }
        }
    }
}
