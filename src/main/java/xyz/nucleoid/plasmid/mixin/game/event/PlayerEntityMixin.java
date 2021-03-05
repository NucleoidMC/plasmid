package xyz.nucleoid.plasmid.mixin.game.event;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.event.DropItemListener;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> ci) {
        if (this.world.isClient) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);
        if (gameSpace != null && gameSpace.containsPlayer(player)) {
            int slot = player.inventory.selectedSlot;
            ItemStack stack = player.inventory.getStack(slot);

            try {
                ActionResult dropResult = gameSpace.invoker(DropItemListener.EVENT).onDrop(player, slot, stack);
                if (dropResult == ActionResult.FAIL) {
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, slot, stack));

                    ci.setReturnValue(false);
                }
            } catch (Throwable t) {
                Plasmid.LOGGER.error("An unexpected exception occurred while dispatching drop item event", t);
                gameSpace.reportError(t, "Dropping item");
            }
        }
    }
}
