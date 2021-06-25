package xyz.nucleoid.plasmid.game.common;

import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;

import java.util.UUID;

public final class OldCombat {
    private static final EntityAttribute DAMAGE_ATTRIBUTE = EntityAttributes.GENERIC_ATTACK_DAMAGE;
    private static final EntityAttribute SPEED_ATTRIBUTE = EntityAttributes.GENERIC_ATTACK_SPEED;

    private static final UUID DAMAGE_ID = Item.ATTACK_DAMAGE_MODIFIER_ID;
    private static final UUID SPEED_ID = Item.ATTACK_SPEED_MODIFIER_ID;

    private static final int HOE_BASE_DAMAGE = 0;
    private static final int SHOVEL_BASE_DAMAGE = 0;
    private static final int PICKAXE_BASE_DAMAGE = 1;
    private static final int AXE_BASE_DAMAGE = 2;
    private static final int SWORD_BASE_DAMAGE = 3;

    public static ItemStack applyTo(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ToolItem)) {
            return stack;
        }

        ToolMaterial material = ((ToolItem) item).getMaterial();

        Multimap<EntityAttribute, EntityAttributeModifier> defaultModifiers = item.getAttributeModifiers(EquipmentSlot.MAINHAND);

        if (defaultModifiers.containsKey(SPEED_ATTRIBUTE)) {
            EntityAttributeModifier modifier = createSpeedModifier();
            stack.addAttributeModifier(SPEED_ATTRIBUTE, modifier, EquipmentSlot.MAINHAND);
        }

        if (defaultModifiers.containsKey(DAMAGE_ATTRIBUTE)) {
            float attackDamage = material.getAttackDamage();
            int baseDamage = getBaseDamage(item);

            EntityAttributeModifier modifier = createDamageModifier(attackDamage + baseDamage);
            stack.addAttributeModifier(DAMAGE_ATTRIBUTE, modifier, EquipmentSlot.MAINHAND);
        }

        return stack;
    }

    private static EntityAttributeModifier createSpeedModifier() {
        return new EntityAttributeModifier(SPEED_ID, "Weapon modifier", 10000.0F, EntityAttributeModifier.Operation.ADDITION);
    }

    private static EntityAttributeModifier createDamageModifier(double damage) {
        return new EntityAttributeModifier(DAMAGE_ID, "Weapon modifier", damage, EntityAttributeModifier.Operation.ADDITION);
    }

    private static int getBaseDamage(Item item) {
        if (item.isIn(FabricToolTags.SWORDS)) {
            return SWORD_BASE_DAMAGE;
        } else if (item.isIn(FabricToolTags.AXES)) {
            return AXE_BASE_DAMAGE;
        } else if (item.isIn(FabricToolTags.PICKAXES)) {
            return PICKAXE_BASE_DAMAGE;
        } else if (item.isIn(FabricToolTags.SHOVELS)) {
            return SHOVEL_BASE_DAMAGE;
        } else if (item.isIn(FabricToolTags.HOES)) {
            return HOE_BASE_DAMAGE;
        }
        return 0;
    }
}
