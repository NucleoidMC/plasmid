package net.gegy1000.plasmid.mixin.rule;

import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private float exhaustion;

    @Shadow
    private float foodSaturationLevel;

    @Inject(method = "update", at = @At("HEAD"))
    private void update(PlayerEntity player, CallbackInfo ci) {
        if (player.world.isClient) {
            return;
        }

        if (this.exhaustion > 4.0F || this.foodSaturationLevel > 0.0F) {
            GameWorld gameWorld = GameWorld.forWorld(player.world);
            if (gameWorld != null && gameWorld.containsPlayer((ServerPlayerEntity) player)) {
                if (gameWorld.testRule(GameRule.ENABLE_HUNGER) == RuleResult.DENY) {
                    this.exhaustion = 0.0F;
                    this.foodSaturationLevel = 0.0F;
                }
            }
        }
    }
}
