package net.gegy1000.plasmid.mixin.rule;

import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void testPvpEnabled(CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        GameWorld gameWorld = GameWorld.forWorld(self.world);
        if (gameWorld != null && gameWorld.containsPlayer(self)) {
            RuleResult result = gameWorld.testRule(GameRule.ALLOW_PVP);
            if (result == RuleResult.ALLOW) {
                ci.setReturnValue(true);
            } else if (result == RuleResult.DENY) {
                ci.setReturnValue(false);
            }
        }
    }
}
