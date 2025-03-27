package xyz.nucleoid.plasmid.mixin.game.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.nucleoid.plasmid.impl.component.PlasmidDataComponentTypes;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    @Final
    private ItemStack activeItemStack;

    @WrapOperation(
            method = "blockedByShield",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"
            )
    )
    private Item allowOldCombatSwordBlocking(ItemStack stack, Operation<Item> operation) {
        // Allow fulfilling the instanceof ShieldItem check
        if (stack.contains(PlasmidDataComponentTypes.OLD_COMBAT)) {
            return Items.SHIELD;
        }

        return operation.call(stack);
    }

    @WrapOperation(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"
            )
    )
    private boolean applyHalfDamageFromSwordBlocking(LivingEntity entity, DamageSource source, Operation<Boolean> operation, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount) {
        if (operation.call(entity, source)) {
            ItemStack stack = entity.getBlockingItem();

            if (!stack.contains(PlasmidDataComponentTypes.OLD_COMBAT) || stack.getItem() instanceof ShieldItem) {
                return true;
            }

            amount.set(Math.max(0, (amount.get() / 2) - 0.5f));
        }

        return false;
    }

    @ModifyConstant(
            method = "getBlockingItem",
            constant = @Constant(intValue = 5)
    )
    private int allowInstantSwordBlocking(int original) {
        if (!this.activeItemStack.contains(PlasmidDataComponentTypes.OLD_COMBAT) || this.activeItemStack.getItem() instanceof ShieldItem) {
            return original;
        }

        return 0;
    }
}
