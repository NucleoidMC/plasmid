package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorld;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleWorld bubble = BubbleWorld.forWorld(player.world);
        if (bubble != null) {
            bubble.removePlayer(player);
        }
    }
}
