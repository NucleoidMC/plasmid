package xyz.nucleoid.plasmid.mixin.game.rule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true)
    private void attemptRegeneration(PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        var gameSpace = GameSpaceManager.get().byPlayer(player);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SATURATED_REGENERATION) == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
