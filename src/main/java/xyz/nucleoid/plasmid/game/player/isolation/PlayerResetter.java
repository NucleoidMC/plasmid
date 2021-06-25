package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public final class PlayerResetter {
    private final CompoundTag resetNbt;

    public PlayerResetter(CompoundTag resetNbt) {
        this.resetNbt = resetNbt;
    }

    public void apply(ServerPlayerEntity player) {
        this.clearAttributeModifiers(player);
        player.clearStatusEffects();
        player.getScoreboardTags().clear();

        player.fromTag(this.resetNbt);
    }

    private void clearAttributeModifiers(ServerPlayerEntity player) {
        AttributeContainer attributes = player.getAttributes();
        for (EntityAttribute attribute : Registry.ATTRIBUTE) {
            if (attributes.hasAttribute(attribute)) {
                EntityAttributeInstance instance = attributes.getCustomInstance(attribute);
                if (instance != null) {
                    this.clearModifiers(instance);
                }
            }
        }
    }

    private void clearModifiers(EntityAttributeInstance instance) {
        for (EntityAttributeModifier modifier : instance.getModifiers()) {
            instance.removeModifier(modifier);
        }
    }
}
