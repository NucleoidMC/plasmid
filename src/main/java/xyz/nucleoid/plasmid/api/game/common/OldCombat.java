package xyz.nucleoid.plasmid.api.game.common;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/**
 * A utility class that allows old-style 1.8 combat to be applied to any given {@link ItemStack}.
 * <p>
 * This works by modifying the damage and attack speed attributes to match their 1.8 levels.
 */
public final class OldCombat {
    private static final Holder<Attribute> DAMAGE_ATTRIBUTE = Attributes.ATTACK_DAMAGE;
    private static final Holder<Attribute> SPEED_ATTRIBUTE = Attributes.ATTACK_SPEED;

    private static final Identifier DAMAGE_ID = Item.BASE_ATTACK_DAMAGE_ID;
    private static final Identifier SPEED_ID = Item.BASE_ATTACK_SPEED_ID;

    private static final ToolMaterial[] TOOL_MATERIALS = {
            ToolMaterial.WOOD,
            ToolMaterial.GOLD,
            ToolMaterial.STONE,
            ToolMaterial.IRON,
            ToolMaterial.DIAMOND,
            ToolMaterial.NETHERITE
    };

    private static final int HOE_BASE_DAMAGE = 0;
    private static final int SHOVEL_BASE_DAMAGE = 0;
    private static final int PICKAXE_BASE_DAMAGE = 1;
    private static final int AXE_BASE_DAMAGE = 2;
    private static final int SWORD_BASE_DAMAGE = 3;

    public static ItemStack applyTo(ItemStack stack) {
        if (!stack.has(DataComponents.TOOL)) {
            return stack;
        }

        var material = getToolMaterial(stack);

        if (material == null) {
            return stack;
        }

        var defaultModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

        {
            AttributeModifier modifier = createSpeedModifier();
            defaultModifiers = defaultModifiers.withModifierAdded(SPEED_ATTRIBUTE, modifier, EquipmentSlotGroup.MAINHAND);
        }

        {
            float attackDamageBonus = stack.is(ItemTags.HOES) ? 0 : getToolMaterial(stack).attackDamageBonus();
            int baseDamage = getBaseDamage(stack);

            AttributeModifier modifier = createDamageModifier(attackDamageBonus + baseDamage);
            defaultModifiers = defaultModifiers.withModifierAdded(DAMAGE_ATTRIBUTE, modifier, EquipmentSlotGroup.MAINHAND);
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, defaultModifiers);
        return stack;
    }

    private static AttributeModifier createSpeedModifier() {
        return new AttributeModifier(SPEED_ID, 10000.0F, AttributeModifier.Operation.ADD_VALUE);
    }

    private static AttributeModifier createDamageModifier(double damage) {
        return new AttributeModifier(DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE);
    }

    private static ToolMaterial getToolMaterial(ItemStack item) {
        for (var material : TOOL_MATERIALS) {
            var repairable = item.get(DataComponents.REPAIRABLE);

            if (repairable != null) {
                var repairItems = repairable.items().unwrapKey();

                if (repairItems.isPresent() && repairItems.get().equals(material.repairItems())) {
                    return material;
                }
            }
        }

        return null;
    }

    private static int getBaseDamage(ItemStack stack) {
        if (stack.is(ItemTags.SWORDS)) {
            return SWORD_BASE_DAMAGE;
        } else if (stack.is(ItemTags.AXES)) {
            return AXE_BASE_DAMAGE;
        } else if (stack.is(ItemTags.PICKAXES)) {
            return PICKAXE_BASE_DAMAGE;
        } else if (stack.is(ItemTags.SHOVELS)) {
            return SHOVEL_BASE_DAMAGE;
        } else if (stack.is(ItemTags.HOES)) {
            return HOE_BASE_DAMAGE;
        }
        return 0;
    }
}
