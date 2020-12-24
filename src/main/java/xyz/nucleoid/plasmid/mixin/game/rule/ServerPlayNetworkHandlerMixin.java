package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(
            method = "onPlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void onPlayerMoveInVehicle(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        // test if this packet contains position data
        if (Double.isNaN(packet.getX(Double.NaN))) {
            return;
        }

        // we're in a vehicle and the player tried to change their position!

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.player.world);
        if (gameSpace != null && gameSpace.testRule(GameRule.DISMOUNT_VEHICLE) == RuleResult.DENY) {
            // the player is probably desynchronized: update them with the vehicle passengers
            Entity vehicle = this.player.getVehicle();
            this.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
        }
    }
}
