package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void testPvpEnabled(CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(self.world);
        if (gameSpace != null && gameSpace.containsPlayer(self)) {
            RuleResult result = gameSpace.testRule(GameRule.PVP);
            if (result == RuleResult.ALLOW) {
                ci.setReturnValue(true);
            } else if (result == RuleResult.DENY) {
                ci.setReturnValue(false);
            }
        }
    }
}
