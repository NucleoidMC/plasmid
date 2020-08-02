package net.gegy1000.plasmid.mixin.rule;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.GameManager;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private float exhaustion;

    @Inject(method = "update", at = @At("HEAD"))
    private void update(PlayerEntity player, CallbackInfo ci) {
        if (this.exhaustion > 4.0F) {
            Game game = GameManager.openGame();
            if (game != null && game.containsPlayer(player)) {
                if (game.testRule(GameRule.ENABLE_HUNGER) == RuleResult.DENY) {
                    this.exhaustion = 0.0F;
                }
            }
        }
    }
}
