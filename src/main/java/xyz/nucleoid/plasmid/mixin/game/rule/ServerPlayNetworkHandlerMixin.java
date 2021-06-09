package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

    @Inject(
            method = "onClickSlot",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.player.world);

        if (gameSpace != null) {
            if (packet.getSlot() < 0 || packet.getSlot() >= this.player.inventory.size()) return;
            // See https://wiki.vg/File:Inventory-slots.png for the slot numbering
            boolean isArmor = (packet.getSlot() >= 5 && packet.getSlot() <= 8) && this.player.currentScreenHandler instanceof PlayerScreenHandler;
            boolean denyModifyInventory = gameSpace.testRule(GameRule.MODIFY_INVENTORY) == RuleResult.DENY;
            RuleResult modifyArmor = gameSpace.testRule(GameRule.MODIFY_ARMOR);
            if ((denyModifyInventory && (!isArmor || modifyArmor != RuleResult.ALLOW))
                    || (isArmor && modifyArmor == RuleResult.DENY)) {
                ItemStack stack = this.player.inventory.getStack(packet.getSlot());

                ci.cancel();
                if (!packet.getStack().isEmpty()) {
                    // this.player.playSound didn't appear to work, but a packet did.
                    this.sendPacket(new PlaySoundS2CPacket(
                            SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.MASTER,
                            this.player.getX(), this.player.getY(), this.player.getZ(),
                            1.0f, 1.0f
                    ));
                }

                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(packet.getSyncId(), packet.getSlot(), stack));
                this.player.refreshScreenHandler(this.player.currentScreenHandler);
                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.inventory.getCursorStack()));
                this.sendPacket(new ConfirmScreenActionS2CPacket(packet.getSyncId(), packet.getActionId(), false));
            }
        }
    }
}
