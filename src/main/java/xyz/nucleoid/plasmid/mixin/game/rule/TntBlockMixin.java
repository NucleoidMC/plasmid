package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
        if (gameSpace != null) {
            RuleResult result = gameSpace.testRule(GameRule.UNSTABLE_TNT);
            if (result == RuleResult.ALLOW) {
                TntBlock.primeTnt(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}
