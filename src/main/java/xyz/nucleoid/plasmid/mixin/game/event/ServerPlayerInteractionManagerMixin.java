package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.PlayerPunchBlockListener;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerWorld world;

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    shift = Shift.BEFORE,
                    target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"
            ),
            cancellable = true
    )
    public void processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);
        if (gameSpace != null) {
            try {
                ActionResult result = gameSpace.invoker(PlayerPunchBlockListener.EVENT).onPunchBlock(this.player, direction, pos);

                if (result == ActionResult.FAIL) {
                    this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(pos, this.world.getBlockState(pos), action, false, ""));
                    ci.cancel();
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching player punch block event", t);
                gameSpace.reportError(t, "Punching block");
            }
        }
    }
}
