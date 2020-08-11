package xyz.nucleoid.plasmid.mixin.map;

import xyz.nucleoid.plasmid.game.GameWorld;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        GameWorld gameWorld = GameWorld.forWorld(player.world);
        if (gameWorld != null) {
            gameWorld.removePlayer(player);
        }
    }
}
