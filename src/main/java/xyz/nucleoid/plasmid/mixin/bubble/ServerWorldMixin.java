package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorld;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void onPlayerAdded(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleWorld bubble = BubbleWorld.forWorld(player.world);
        if (bubble != null) {
            bubble.addPlayer(player);
        }
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void onPlayerRemoved(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleWorld bubble = BubbleWorld.forWorld(player.world);
        if (bubble != null) {
            bubble.removePlayer(player);
        }
    }
}
