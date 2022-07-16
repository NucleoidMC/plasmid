package xyz.nucleoid.plasmid.mixin.game.rule;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, PlayerPublicKey publicKey) {
        super(world, pos, yaw, profile, publicKey);
    }

    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void allowPvPInGames(CallbackInfoReturnable<Boolean> cir) {
        var gameSpace = GameSpaceManager.get().byPlayer(this);
        if (gameSpace != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void stopRiding(CallbackInfo ci) {
        var vehicle = this.getVehicle();
        if (vehicle == null) {
            // how did we get here?
            return;
        }

        if (!this.world.isClient()) {
            var gameSpace = GameSpaceManager.get().byPlayer(this);
            if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.DISMOUNT_VEHICLE) == ActionResult.FAIL) {
                ci.cancel();
                this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
            }
        }
    }
}
