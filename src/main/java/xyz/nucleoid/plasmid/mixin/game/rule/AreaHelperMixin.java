package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.AreaHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(AreaHelper.class)
public class AreaHelperMixin {
    @Shadow
    @Final
    private WorldAccess world;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void isValid(CallbackInfoReturnable<Boolean> ci) {
        if (!(this.world instanceof ServerWorldAccess)) {
            return;
        }

        ServerWorld serverWorld = ((ServerWorldAccess) this.world).toServerWorld();
        GameWorld gameWorld = GameWorld.forWorld(serverWorld);
        if (gameWorld != null) {
            RuleResult result = gameWorld.testRule(GameRule.PORTALS);
            if (result == RuleResult.DENY) {
                ci.setReturnValue(false);
            }
        }
    }
}
