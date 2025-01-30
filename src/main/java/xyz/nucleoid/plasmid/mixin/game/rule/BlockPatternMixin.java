package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(BlockPattern.class)
public class BlockPatternMixin {
    @Inject(
            method = "searchAround",
            at = @At("HEAD")
    )
    private void applyPortalsRuleToEndPortals(WorldView worldView, BlockPos pos, CallbackInfoReturnable<BlockPattern.Result> ci) {
        if (!(worldView instanceof World world) || ((BlockPattern) (Object) this) != EndPortalFrameBlock.getCompletedFramePattern()) {
            return;
        }

        var gameSpace = GameSpaceManagerImpl.get().byWorld(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.PORTALS) == EventResult.DENY) {
            ci.setReturnValue(null);
        }
    }
}
