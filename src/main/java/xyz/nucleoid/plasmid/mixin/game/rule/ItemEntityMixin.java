package xyz.nucleoid.plasmid.mixin.game.rule;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ItemEntity;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Shadow
    @Final
    private int pickupDelay;

    @Redirect(method = "onPlayerCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ItemEntity;pickupDelay:I"))
    private int applyCollisionItemsGameRule(ItemEntity self) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(self.world);
        if (gameSpace != null && gameSpace.testRule(GameRule.PICKUP_ITEMS) == RuleResult.DENY) {
            // pickupDelay != 0 prevents pickup
            return 1;
        }

        return this.pickupDelay;
    }

    @Inject(method = "cannotPickup", at = @At("HEAD"), cancellable = true)
    private void applyExternalPickupItemsGameRule(CallbackInfoReturnable<Boolean> ci) {
        ItemEntity self = (ItemEntity) (Object) this;

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(self.world);
        if (gameSpace != null && gameSpace.testRule(GameRule.PICKUP_ITEMS) == RuleResult.DENY) {
            ci.setReturnValue(true);
        }
    }
}