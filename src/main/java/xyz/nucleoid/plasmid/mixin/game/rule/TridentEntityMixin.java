package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ThrownTrident.class)
public abstract class TridentEntityMixin extends AbstractArrow {
    @Shadow
    private boolean dealtDamage;

    protected TridentEntityMixin(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!this.dealtDamage && this.getY() <= this.level().getMinY() && !this.level().isClientSide()) {
            var gameSpace = GameSpaceManagerImpl.get().byLevel(this.level());
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.TRIDENTS_LOYAL_IN_VOID) == EventResult.ALLOW) {
                this.dealtDamage = true;
                this.setDeltaMovement(0.0, 0.0, 0.0);
            }
        }
    }
}
