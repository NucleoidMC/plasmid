package net.gegy1000.plasmid.mixin.event;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.GameManager;
import net.gegy1000.plasmid.game.event.ExplosionListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Final
    @Shadow
    private double x;
    @Final
    @Shadow
    private double y;
    @Final
    @Shadow
    private double z;

    @Final
    @Shadow
    private List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void affectWorld(boolean blocks, CallbackInfo ci) {
        Game game = GameManager.openGame();
        if (game != null && game.containsPos(this.pos())) {
            game.invoker(ExplosionListener.EVENT).onExplosion(game, this.affectedBlocks);
        }
    }

    private BlockPos pos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
