package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            var gameSpace = GameSpaceManagerImpl.get().byPlayer((PlayerEntity) (Object) this);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK) == EventResult.ALLOW) {
                cir.setReturnValue(super.damage(world, source, amount));
            }
        }
    }

    @Inject(method = "dismountVehicle", at = @At("HEAD"), cancellable = true)
    private void dismountVehicle(CallbackInfo ci) {
        var vehicle = this.getVehicle();
        if (vehicle == null || vehicle.isRemoved()) {
            // how did we get here?
            return;
        }

        if (!this.getEntityWorld().isClient()) {
            var serverPlayer = (ServerPlayerEntity) (Object) this;

            var gameSpace = GameSpaceManagerImpl.get().byPlayer(serverPlayer);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.DISMOUNT_VEHICLE) == EventResult.DENY) {
                ci.cancel();
            }
        }
    }
}
