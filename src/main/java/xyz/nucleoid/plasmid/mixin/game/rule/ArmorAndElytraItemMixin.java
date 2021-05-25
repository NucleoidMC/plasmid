package xyz.nucleoid.plasmid.mixin.game.rule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin({ ArmorItem.class, ElytraItem.class })
public class ArmorAndElytraItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        if (world.isClient) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
        if (gameSpace != null && gameSpace.testRule(GameRule.MODIFY_ARMOR) == RuleResult.DENY) {
            ItemStack stack = user.getStackInHand(hand);
            ci.setReturnValue(TypedActionResult.fail(stack));

            user.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1.0F, 1.0F);
        }
    }
}
