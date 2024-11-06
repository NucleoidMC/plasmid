package xyz.nucleoid.plasmid.mixin.game.rule;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void allowPvPInGames(CallbackInfoReturnable<Boolean> cir) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(this);
        if (gameSpace != null) {
            cir.setReturnValue(true);
        }
    }
}
