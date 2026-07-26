package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "hurtServer", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void damage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            var gameSpace = GameSpaceManagerImpl.get().byPlayer((Player) (Object) this);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK) == EventResult.ALLOW) {
                cir.setReturnValue(super.hurtServer(world, source, amount));
            }
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"), cancellable = true)
    private void dismountVehicle(CallbackInfo ci) {
        var vehicle = this.getVehicle();
        if (vehicle == null || vehicle.isRemoved()) {
            // how did we get here?
            return;
        }

        if (!this.level().isClientSide()) {
            var serverPlayer = (ServerPlayer) (Object) this;

            var gameSpace = GameSpaceManagerImpl.get().byPlayer(serverPlayer);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.DISMOUNT_VEHICLE) == EventResult.DENY) {
                ci.cancel();
            }
        }
    }
}
