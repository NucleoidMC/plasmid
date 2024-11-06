package xyz.nucleoid.plasmid.mixin.game.rule;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @WrapOperation(
            method = "update",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;saturationLevel:F", opcode = Opcodes.GETFIELD, ordinal = 2)
    )
    private float attemptSaturatedRegeneration(HungerManager instance, Operation<Float> original, ServerPlayerEntity player) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SATURATED_REGENERATION) == EventResult.DENY) {
            return 0;
        }

        return original.call(instance);
    }
}
