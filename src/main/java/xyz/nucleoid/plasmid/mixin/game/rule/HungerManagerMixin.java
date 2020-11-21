package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

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
            ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(player.world);
            if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                if (gameSpace.testRule(GameRule.HUNGER) == RuleResult.DENY) {
                    this.exhaustion = 0.0F;
                    this.foodSaturationLevel = 0.0F;
                }
            }
        }
    }
}
