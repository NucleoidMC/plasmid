package xyz.nucleoid.plasmid.mixin.game.rule;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;saturationLevel:F", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float attemptSaturatedRegeneration(HungerManager manager, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            var gameSpace = GameSpaceManager.get().byPlayer(player);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SATURATED_REGENERATION) == ActionResult.FAIL) {
                return 0;
            }
        }

        return manager.getSaturationLevel();
    }
}
