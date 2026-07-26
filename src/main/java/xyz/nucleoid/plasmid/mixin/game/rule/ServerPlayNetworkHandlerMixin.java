package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(
            method = "handleContainerClick",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onClickSlot(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(this.player);

        if (gameSpace != null) {
            if (packet.slotNum() < 0 || packet.slotNum() >= this.player.getInventory().getContainerSize()) return;
            // See https://wiki.vg/File:Inventory-slots.png for the slot numbering
            var screenHandler = this.player.containerMenu;

            boolean isArmor = (packet.slotNum() >= 5 && packet.slotNum() <= 8) && screenHandler instanceof InventoryMenu;
            boolean denyModifyInventory = gameSpace.getBehavior().testRule(GameRuleType.MODIFY_INVENTORY) == EventResult.DENY;
            var modifyArmor = gameSpace.getBehavior().testRule(GameRuleType.MODIFY_ARMOR);
            if ((denyModifyInventory && (!isArmor || modifyArmor != EventResult.ALLOW))
                    || (isArmor && modifyArmor == EventResult.DENY)) {
                var stack = screenHandler.getSlot(packet.slotNum()).getItem();

                this.send(new ClientboundContainerSetSlotPacket(packet.containerId(), screenHandler.incrementStateId(), packet.slotNum(), stack));
                this.send(new ClientboundSetCursorItemPacket(screenHandler.getCarried()));

                ci.cancel();
            }
        }
    }
}
