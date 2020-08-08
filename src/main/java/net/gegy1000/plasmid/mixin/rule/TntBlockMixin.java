package net.gegy1000.plasmid.mixin.rule;

import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        GameWorld gameWorld = GameWorld.forWorld(world);
        if (gameWorld != null) {
            RuleResult result = gameWorld.testRule(GameRule.INSTANT_LIGHT_TNT);
            if (result == RuleResult.ALLOW) {
                TntBlock.primeTnt(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}
