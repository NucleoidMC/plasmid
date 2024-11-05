package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow
    private boolean dealtDamage;

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!this.dealtDamage && this.getY() <= this.getWorld().getBottomY() && !this.getWorld().isClient()) {
            var gameSpace = GameSpaceManager.get().byWorld(this.getWorld());
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.TRIDENTS_LOYAL_IN_VOID) == EventResult.ALLOW) {
                this.dealtDamage = true;
                this.setVelocity(0.0, 0.0, 0.0);
            }
        }
    }
}
