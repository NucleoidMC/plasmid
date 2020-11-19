package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import java.util.List;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Shadow
    @Final
    public List<Slot> slots;

    @Inject(method = "method_30010", at = @At("HEAD"), cancellable = true)
    private void onSlotAction(int slot, int data, SlotActionType type, PlayerEntity player, CallbackInfoReturnable<ItemStack> ci) {
        if (player.world.isClient) {
            return;
        }

        if (type == SlotActionType.THROW || type == SlotActionType.PICKUP) {
            if (this.shouldBlockThrowingItems(player)) {
                if (type == SlotActionType.PICKUP && slot == -999) {
                    ci.setReturnValue(player.inventory.getCursorStack());
                } else if (type == SlotActionType.THROW && slot >= 0 && slot < this.slots.size()) {
                    ci.setReturnValue(this.slots.get(slot).getStack());
                }
            }
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void close(PlayerEntity player, CallbackInfo ci) {
        ItemStack cursor = player.inventory.getCursorStack();
        if (cursor.isEmpty()) {
            return;
        }

        if (this.shouldBlockThrowingItems(player)) {
            if (player.inventory.insertStack(cursor)) {
                player.inventory.setCursorStack(ItemStack.EMPTY);
            }
        }
    }

    private boolean shouldBlockThrowingItems(PlayerEntity player) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(player.world);
        if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
            return gameSpace.testRule(GameRule.THROW_ITEMS) == RuleResult.DENY;
        }
        return false;
    }
}
