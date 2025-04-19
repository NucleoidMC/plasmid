package xyz.nucleoid.plasmid.api.game.common;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/**
 * A utility class that allows old-style 1.8 combat to be applied to any given {@link ItemStack}.
 * <p>
 * This works by modifying the damage and attack speed attributes to match their 1.8 levels.
 */
public final class OldCombat {
    private static final RegistryEntry<EntityAttribute> DAMAGE_ATTRIBUTE = EntityAttributes.ATTACK_DAMAGE;
    private static final RegistryEntry<EntityAttribute> SPEED_ATTRIBUTE = EntityAttributes.ATTACK_SPEED;

    private static final Identifier DAMAGE_ID = Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
    private static final Identifier SPEED_ID = Item.BASE_ATTACK_SPEED_MODIFIER_ID;

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
        if (!stack.contains(DataComponentTypes.TOOL)) {
            return stack;
        }

        var material = getToolMaterial(stack);

        if (material == null) {
            return stack;
        }

        var defaultModifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        {
            EntityAttributeModifier modifier = createSpeedModifier();
            defaultModifiers = defaultModifiers.with(SPEED_ATTRIBUTE, modifier, AttributeModifierSlot.MAINHAND);
        }

        {
            float attackDamageBonus = stack.isIn(ItemTags.HOES) ? 0 : getToolMaterial(stack).attackDamageBonus();
            int baseDamage = getBaseDamage(stack);

            EntityAttributeModifier modifier = createDamageModifier(attackDamageBonus + baseDamage);
            defaultModifiers = defaultModifiers.with(DAMAGE_ATTRIBUTE, modifier, AttributeModifierSlot.MAINHAND);
        }

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, defaultModifiers);
        return stack;
    }

    private static EntityAttributeModifier createSpeedModifier() {
        return new EntityAttributeModifier(SPEED_ID, 10000.0F, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    private static EntityAttributeModifier createDamageModifier(double damage) {
        return new EntityAttributeModifier(DAMAGE_ID, damage, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    private static ToolMaterial getToolMaterial(ItemStack item) {
        for (var material : TOOL_MATERIALS) {
            var repairable = item.get(DataComponentTypes.REPAIRABLE);

            if (repairable != null) {
                var repairItems = repairable.items().getTagKey();

                if (repairItems.isPresent() && repairItems.get().equals(material.repairItems())) {
                    return material;
                }
            }
        }

        return null;
    }

    private static int getBaseDamage(ItemStack stack) {
        if (stack.isIn(ItemTags.SWORDS)) {
            return SWORD_BASE_DAMAGE;
        } else if (stack.isIn(ItemTags.AXES)) {
            return AXE_BASE_DAMAGE;
        } else if (stack.isIn(ItemTags.PICKAXES)) {
            return PICKAXE_BASE_DAMAGE;
        } else if (stack.isIn(ItemTags.SHOVELS)) {
            return SHOVEL_BASE_DAMAGE;
        } else if (stack.isIn(ItemTags.HOES)) {
            return HOE_BASE_DAMAGE;
        }
        return 0;
    }
}
