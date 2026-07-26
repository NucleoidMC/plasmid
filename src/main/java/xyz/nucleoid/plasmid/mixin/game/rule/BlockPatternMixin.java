package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
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
            method = "find",
            at = @At("HEAD")
    )
    private void applyPortalsRuleToEndPortals(LevelReader worldView, BlockPos pos, CallbackInfoReturnable<BlockPattern.BlockPatternMatch> ci) {
        if (!(worldView instanceof Level world) || ((BlockPattern) (Object) this) != EndPortalFrameBlock.getOrCreatePortalShape()) {
            return;
        }

        var gameSpace = GameSpaceManagerImpl.get().byLevel(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.PORTALS) == EventResult.DENY) {
            ci.setReturnValue(null);
        }
    }
}
