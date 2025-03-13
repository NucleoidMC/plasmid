package xyz.nucleoid.plasmid.test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.plasmid.api.game.common.OldCombat;

import static org.junit.jupiter.api.Assertions.*;

public class OldCombatTests {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    public void testAttributes() {
        // Swords
        assertAttributeModifiers(Items.WOODEN_SWORD, 3);
        assertAttributeModifiers(Items.GOLDEN_SWORD, 3);
        assertAttributeModifiers(Items.STONE_SWORD, 4);
        assertAttributeModifiers(Items.IRON_SWORD, 5);
        assertAttributeModifiers(Items.DIAMOND_SWORD, 6);
        assertAttributeModifiers(Items.NETHERITE_SWORD, 7);

        // Pickaxes
        assertAttributeModifiers(Items.WOODEN_PICKAXE, 1);
        assertAttributeModifiers(Items.GOLDEN_PICKAXE, 1);
        assertAttributeModifiers(Items.STONE_PICKAXE, 2);
        assertAttributeModifiers(Items.IRON_PICKAXE, 3);
        assertAttributeModifiers(Items.DIAMOND_PICKAXE, 4);
        assertAttributeModifiers(Items.NETHERITE_PICKAXE, 5);

        // Axes
        assertAttributeModifiers(Items.WOODEN_AXE, 2);
        assertAttributeModifiers(Items.GOLDEN_AXE, 2);
        assertAttributeModifiers(Items.STONE_AXE, 3);
        assertAttributeModifiers(Items.IRON_AXE, 4);
        assertAttributeModifiers(Items.DIAMOND_AXE, 5);
        assertAttributeModifiers(Items.NETHERITE_AXE, 6);

        // Shovels
        assertAttributeModifiers(Items.WOODEN_SHOVEL, 0);
        assertAttributeModifiers(Items.GOLDEN_SHOVEL, 0);
        assertAttributeModifiers(Items.STONE_SHOVEL, 1);
        assertAttributeModifiers(Items.IRON_SHOVEL, 2);
        assertAttributeModifiers(Items.DIAMOND_SHOVEL, 3);
        assertAttributeModifiers(Items.NETHERITE_SHOVEL, 4);

        // Hoes
        assertAttributeModifiers(Items.WOODEN_HOE, 0);
        assertAttributeModifiers(Items.GOLDEN_HOE, 0);
        assertAttributeModifiers(Items.STONE_HOE, 0);
        assertAttributeModifiers(Items.IRON_HOE, 0);
        assertAttributeModifiers(Items.DIAMOND_HOE, 0);
        assertAttributeModifiers(Items.NETHERITE_HOE, 0);
    }

    private static void assertAttributeModifiers(Item item, float expectedAttackDamage) {
        var stack = OldCombat.applyTo(new ItemStack(item));
        var component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        // Attack speed
        var attackSpeed = getAttributeModifierEntry(component, EntityAttributes.GENERIC_ATTACK_SPEED);
        assertNotNull(attackSpeed, "Missing attack speed attribute modifier for " + item);

        assertEquals(AttributeModifierSlot.MAINHAND, attackSpeed.slot(), "Incorrect attack speed attribute modifier slot for " + item);
        assertEquals(EntityAttributeModifier.Operation.ADD_VALUE, attackSpeed.modifier().operation(), "Incorrect attack speed attribute modifier operation for " + item);
        assertEquals(10000.0F, attackSpeed.modifier().value(), "Incorrect attack speed attribute modifier value for " + item);

        // Attack damage
        var attackDamage = getAttributeModifierEntry(component, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        assertNotNull(attackDamage, "Missing attack damage attribute modifier for " + item);

        assertEquals(AttributeModifierSlot.MAINHAND, attackDamage.slot(), "Incorrect attack damage attribute modifier slot for " + item);
        assertEquals(EntityAttributeModifier.Operation.ADD_VALUE, attackDamage.modifier().operation(), "Incorrect attack damage attribute modifier operation for " + item);
        assertEquals(expectedAttackDamage, attackDamage.modifier().value(), "Incorrect attack damage attribute modifier value for " + item);
    }

    private static AttributeModifiersComponent.Entry getAttributeModifierEntry(AttributeModifiersComponent component, RegistryEntry<EntityAttribute> attribute) {
        for (var entry : component.modifiers()) {
            if (attribute == entry.attribute()) {
                return entry;
            }
        }

        return null;
    }
}
