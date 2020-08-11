package xyz.nucleoid.plasmid.mixin.rule;

import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Inject(method = "createPortalAt", at = @At("HEAD"), cancellable = true)
    private static void handleCreatePortal(WorldAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        GameWorld gameWorld = GameWorld.forWorld(world.getWorld());
        if (gameWorld != null) {
            RuleResult result = gameWorld.testRule(GameRule.ALLOW_PORTALS);
            if (result == RuleResult.DENY) {
                ci.setReturnValue(false);
            }
        }
    }
}
