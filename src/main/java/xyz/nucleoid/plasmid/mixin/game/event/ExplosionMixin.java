package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.ExplosionListener;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow
    @Final
    private World world;

    @Final
    @Shadow
    private List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void affectWorld(boolean blocks, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);
        if (gameSpace != null) {
            gameSpace.invoker(ExplosionListener.EVENT).onExplosion(this.affectedBlocks);
        }
    }
}
