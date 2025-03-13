package xyz.nucleoid.plasmid.mixin.game.rule;

//import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;

/*@Mixin(EquippableComponent.class)
public class EquippableComponentMixin {
    @Inject(method = "equip", at = @At("HEAD"), cancellable = true)
    private void equip(ItemStack stack, PlayerEntity user, CallbackInfoReturnable<ActionResult> ci) {
        if (!(user instanceof ServerPlayerEntity)) {
            return;
        }

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(user);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.MODIFY_ARMOR) == EventResult.DENY) {
            ci.setReturnValue(ActionResult.FAIL);
        }
    }
}*/
