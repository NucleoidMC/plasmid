package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(Equippable.class)
public class EquippableComponentMixin {
    @Inject(method = "swapWithEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void equip(ItemStack stack, Player user, CallbackInfoReturnable<InteractionResult> ci) {
        if (!(user instanceof ServerPlayer)) {
            return;
        }

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(user);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.MODIFY_ARMOR) == EventResult.DENY) {
            ci.setReturnValue(InteractionResult.FAIL);
        }
    }
}
