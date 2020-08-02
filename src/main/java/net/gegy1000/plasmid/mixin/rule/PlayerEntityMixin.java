package net.gegy1000.plasmid.mixin.rule;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.GameManager;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
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

        Game game = GameManager.openGame();
        if (game != null && game.containsPlayer(self)) {
            RuleResult result = game.testRule(GameRule.FALL_DAMAGE);
            if (result == RuleResult.ALLOW) {
                ci.setReturnValue(false);
            } else if (result == RuleResult.DENY) {
                ci.setReturnValue(true);
            }
        }
    }
}
