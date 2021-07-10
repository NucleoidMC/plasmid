package xyz.nucleoid.plasmid.mixin.game.rule;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

@Mixin({ ArmorItem.class, ElytraItem.class })
public class ArmorAndElytraItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        if (world.isClient()) {
            return;
        }

        var gameSpace = GameSpaceManager.get().byPlayer(user);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.MODIFY_ARMOR) == ActionResult.FAIL) {
            var stack = user.getStackInHand(hand);
            ci.setReturnValue(TypedActionResult.fail(stack));

            user.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1.0F, 1.0F);
        }
    }
}
