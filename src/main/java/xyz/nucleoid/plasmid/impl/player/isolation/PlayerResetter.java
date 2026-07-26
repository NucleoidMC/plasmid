package xyz.nucleoid.plasmid.impl.player.isolation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import xyz.nucleoid.plasmid.api.util.InventoryUtil;

public final class PlayerResetter {
    private final CompoundTag resetNbt;

    public PlayerResetter(CompoundTag resetNbt) {
        this.resetNbt = resetNbt;
    }

    public void apply(ServerPlayer player) {
        this.clearAttributeModifiers(player);
        player.removeAllEffects();
        player.entityTags().clear();
        InventoryUtil.clear(player);
        player.load(TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), this.resetNbt));
    }

    private void clearAttributeModifiers(ServerPlayer player) {
        var attributes = player.getAttributes();
        for (var attribute : BuiltInRegistries.ATTRIBUTE.asHolderIdMap()) {
            if (attributes.hasAttribute(attribute)) {
                var instance = attributes.getInstance(attribute);
                if (instance != null) {
                    instance.removeModifiers();
                }
            }
        }
    }
}
