package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

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

        var gameSpace = GameSpaceManager.get().byPlayer(this.player);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.DISMOUNT_VEHICLE) == ActionResult.FAIL) {
            /* the player is probably desynchronized: update them with the vehicle passengers */
            var vehicle = this.player.getVehicle();
            this.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
        }
    }

    @Inject(
            method = "onClickSlot",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        var gameSpace = GameSpaceManager.get().byPlayer(this.player);

        if (gameSpace != null) {
            if (packet.getSlot() < 0 || packet.getSlot() >= this.player.getInventory().size()) return;
            // See https://wiki.vg/File:Inventory-slots.png for the slot numbering
            var screenHandler = this.player.currentScreenHandler;

            boolean isArmor = (packet.getSlot() >= 5 && packet.getSlot() <= 8) && screenHandler instanceof PlayerScreenHandler;
            boolean denyModifyInventory = gameSpace.getBehavior().testRule(GameRuleType.MODIFY_INVENTORY) == ActionResult.FAIL;
            var modifyArmor = gameSpace.getBehavior().testRule(GameRuleType.MODIFY_ARMOR);
            if ((denyModifyInventory && (!isArmor || modifyArmor != ActionResult.SUCCESS))
                    || (isArmor && modifyArmor == ActionResult.FAIL)) {
                var stack = screenHandler.getSlot(packet.getSlot()).getStack();

                // I feel any sounds/etc should be handled by games, not plasmid. Keeping the code so it's easier to revert if needed
                /*if (!packet.getStack().isEmpty()) {
                    // this.player.playSound didn't appear to work, but a packet did.
                    this.sendPacket(new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_VILLAGER_NO), SoundCategory.MASTER,
                            this.player.getX(), this.player.getY(), this.player.getZ(),
                            1.0f, 1.0f,
                            this.player.getRandom().nextLong()
                    ));
                }*/

                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(packet.getSyncId(), screenHandler.nextRevision(), packet.getSlot(), stack));
                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_CURSOR_SYNC_ID, screenHandler.nextRevision(), -1, screenHandler.getCursorStack()));

                ci.cancel();
            }
        }
    }
}
