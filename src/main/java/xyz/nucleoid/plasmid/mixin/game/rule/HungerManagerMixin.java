package xyz.nucleoid.plasmid.mixin.game.rule;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(FoodData.class)
public class HungerManagerMixin {
    @WrapOperation(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;saturationLevel:F", opcode = Opcodes.GETFIELD, ordinal = 2)
    )
    private float attemptSaturatedRegeneration(FoodData instance, Operation<Float> original, ServerPlayer player) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.SATURATED_REGENERATION) == EventResult.DENY) {
            return 0;
        }

        return original.call(instance);
    }
}
