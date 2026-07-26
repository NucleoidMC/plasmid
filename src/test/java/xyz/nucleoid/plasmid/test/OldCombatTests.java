package xyz.nucleoid.plasmid.test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import xyz.nucleoid.plasmid.api.game.common.OldCombat;
import xyz.nucleoid.plasmid.mixin.DataComponentInitializersAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OldCombatTests {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Work around tags not being bound
        bindTag(Items.WOODEN_SWORD, ItemTags.SWORDS);
        bindTag(Items.GOLDEN_SWORD, ItemTags.SWORDS);
        bindTag(Items.STONE_SWORD, ItemTags.SWORDS);
        bindTag(Items.IRON_SWORD, ItemTags.SWORDS);
        bindTag(Items.DIAMOND_SWORD, ItemTags.SWORDS);
        //bindTag(Items.NETHERITE_SWORD, ItemTags.SWORDS);

        bindTag(Items.WOODEN_PICKAXE, ItemTags.PICKAXES);
        bindTag(Items.GOLDEN_PICKAXE, ItemTags.PICKAXES);
        bindTag(Items.STONE_PICKAXE, ItemTags.PICKAXES);
        bindTag(Items.IRON_PICKAXE, ItemTags.PICKAXES);
        bindTag(Items.DIAMOND_PICKAXE, ItemTags.PICKAXES);
        //bindTag(Items.NETHERITE_PICKAXE, ItemTags.PICKAXES);

        bindTag(Items.WOODEN_AXE, ItemTags.AXES);
        bindTag(Items.GOLDEN_AXE, ItemTags.AXES);
        bindTag(Items.STONE_AXE, ItemTags.AXES);
        bindTag(Items.IRON_AXE, ItemTags.AXES);
        bindTag(Items.DIAMOND_AXE, ItemTags.AXES);
        //bindTag(Items.NETHERITE_AXE, ItemTags.AXES);

        bindTag(Items.WOODEN_SHOVEL, ItemTags.SHOVELS);
        bindTag(Items.GOLDEN_SHOVEL, ItemTags.SHOVELS);
        bindTag(Items.STONE_SHOVEL, ItemTags.SHOVELS);
        bindTag(Items.IRON_SHOVEL, ItemTags.SHOVELS);
        bindTag(Items.DIAMOND_SHOVEL, ItemTags.SHOVELS);
        //bindTag(Items.NETHERITE_SHOVEL, ItemTags.SHOVELS);

        bindTag(Items.WOODEN_HOE, ItemTags.HOES);
        bindTag(Items.GOLDEN_HOE, ItemTags.HOES);
        bindTag(Items.STONE_HOE, ItemTags.HOES);
        bindTag(Items.IRON_HOE, ItemTags.HOES);
        bindTag(Items.DIAMOND_HOE, ItemTags.HOES);
        //bindTag(Items.NETHERITE_HOE, ItemTags.HOES);
    }

    @Test
    public void testAttributes() {
        // Swords
        assertAttributeModifiers(Items.WOODEN_SWORD, 3);
        assertAttributeModifiers(Items.GOLDEN_SWORD, 3);
        assertAttributeModifiers(Items.STONE_SWORD, 4);
        assertAttributeModifiers(Items.IRON_SWORD, 5);
        assertAttributeModifiers(Items.DIAMOND_SWORD, 6);
        //assertAttributeModifiers(Items.NETHERITE_SWORD, 7);

        // Pickaxes
        assertAttributeModifiers(Items.WOODEN_PICKAXE, 1);
        assertAttributeModifiers(Items.GOLDEN_PICKAXE, 1);
        assertAttributeModifiers(Items.STONE_PICKAXE, 2);
        assertAttributeModifiers(Items.IRON_PICKAXE, 3);
        assertAttributeModifiers(Items.DIAMOND_PICKAXE, 4);
        //assertAttributeModifiers(Items.NETHERITE_PICKAXE, 5);

        // Axes
        assertAttributeModifiers(Items.WOODEN_AXE, 2);
        assertAttributeModifiers(Items.GOLDEN_AXE, 2);
        assertAttributeModifiers(Items.STONE_AXE, 3);
        assertAttributeModifiers(Items.IRON_AXE, 4);
        assertAttributeModifiers(Items.DIAMOND_AXE, 5);
        //assertAttributeModifiers(Items.NETHERITE_AXE, 6);

        // Shovels
        assertAttributeModifiers(Items.WOODEN_SHOVEL, 0);
        assertAttributeModifiers(Items.GOLDEN_SHOVEL, 0);
        assertAttributeModifiers(Items.STONE_SHOVEL, 1);
        assertAttributeModifiers(Items.IRON_SHOVEL, 2);
        assertAttributeModifiers(Items.DIAMOND_SHOVEL, 3);
        //assertAttributeModifiers(Items.NETHERITE_SHOVEL, 4);

        // Hoes
        assertAttributeModifiers(Items.WOODEN_HOE, 0);
        assertAttributeModifiers(Items.GOLDEN_HOE, 0);
        assertAttributeModifiers(Items.STONE_HOE, 0);
        assertAttributeModifiers(Items.IRON_HOE, 0);
        assertAttributeModifiers(Items.DIAMOND_HOE, 0);
        //assertAttributeModifiers(Items.NETHERITE_HOE, 0);
    }

    @SuppressWarnings("deprecation")
    private static void bindTag(Item item, TagKey<Item> tag) {
        var entry = item.builtInRegistryHolder();
        var id = entry.key();
        entry.bindTags(Set.of(tag));
        var builder = DataComponentMap.builder();

        //noinspection unchecked
        ((DataComponentInitializersAccessor) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS)
                .getInitializers().stream().filter(x -> x.key().identifier().equals(id.identifier()))
                        .findAny().orElseThrow().initializer().run(builder,
                        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), (ResourceKey) id);

        entry.bindComponents(builder.build());
    }

    private static void assertAttributeModifiers(Item item, float expectedAttackDamage) {
        var stack = OldCombat.applyTo(new ItemStack(item));
        var component = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

        // Attack speed
        var attackSpeed = getAttributeModifierEntry(component, Attributes.ATTACK_SPEED);
        assertNotNull(attackSpeed, "Missing attack speed attribute modifier for " + item);

        assertEquals(EquipmentSlotGroup.MAINHAND, attackSpeed.slot(), "Incorrect attack speed attribute modifier slot for " + item);
        assertEquals(AttributeModifier.Operation.ADD_VALUE, attackSpeed.modifier().operation(), "Incorrect attack speed attribute modifier operation for " + item);
        assertEquals(10000.0F, attackSpeed.modifier().amount(), "Incorrect attack speed attribute modifier value for " + item);

        // Attack damage
        var attackDamage = getAttributeModifierEntry(component, Attributes.ATTACK_DAMAGE);
        assertNotNull(attackDamage, "Missing attack damage attribute modifier for " + item);

        assertEquals(EquipmentSlotGroup.MAINHAND, attackDamage.slot(), "Incorrect attack damage attribute modifier slot for " + item);
        assertEquals(AttributeModifier.Operation.ADD_VALUE, attackDamage.modifier().operation(), "Incorrect attack damage attribute modifier operation for " + item);
        assertEquals(expectedAttackDamage, attackDamage.modifier().amount(), "Incorrect attack damage attribute modifier value for " + item);
    }

    private static ItemAttributeModifiers.Entry getAttributeModifierEntry(ItemAttributeModifiers component, Holder<Attribute> attribute) {
        for (var entry : component.modifiers()) {
            if (attribute == entry.attribute()) {
                return entry;
            }
        }

        return null;
    }
}
