package xyz.nucleoid.plasmid.mixin.game.space;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.integrated.IntegratedPlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(IntegratedPlayerManager.class)
public abstract class IntegratedPlayerManagerMixin {

    @ModifyExpressionValue(
            method = "savePlayerData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isHost(Lcom/mojang/authlib/GameProfile;)Z")
    )
    private boolean canSavePlayerData(boolean original, ServerPlayerEntity player) {
        return original && !GameSpaceManager.get().inGame(player);
    }
}
