package xyz.nucleoid.plasmid.mixin.fix;

import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: possibly extract into an external library that we include
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Fix an inventory desync bug when the player's inventory is updated before network handling but after player ticking.
     *
     * To avoid sending updates for predictable inventory actions, vanilla flushes to the tracked client state, treating
     * the existing inventory state as what the client is aware of. This tracked client state is used to update the
     * client when the inventory changes server-side (handled by ScreenHandler#sendContentUpdates).
     *
     * This is generally fine, but if the inventory has since changed and those updates have not been sent to the client
     * yet, those updates are consumed and never send to the client.
     *
     * Here, we detect and send all inventory updates to the client before processing the slot click to make sure no
     * clicks are lost.
     *
     * @author Gegy
     */
    @Inject(
            method = "onClickWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;",
                    shift = At.Shift.BEFORE
            )
    )
    private void onClickSlot(ClickWindowC2SPacket packet, CallbackInfo ci) {
        this.player.currentScreenHandler.sendContentUpdates();
    }
}
