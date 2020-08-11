package xyz.nucleoid.plasmid.mixin.rule;

import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.world.isClient || source != DamageSource.FALL) {
            return;
        }

        GameWorld gameWorld = GameWorld.forWorld(self.world);
        if (gameWorld != null && gameWorld.containsPlayer((ServerPlayerEntity) self)) {
            RuleResult result = gameWorld.testRule(GameRule.FALL_DAMAGE);
            if (result == RuleResult.ALLOW) {
                ci.setReturnValue(false);
            } else if (result == RuleResult.DENY) {
                ci.setReturnValue(true);
            }
        }
    }
}
