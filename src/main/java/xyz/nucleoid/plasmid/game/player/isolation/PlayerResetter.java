package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.util.InventoryUtil;

public final class PlayerResetter {
    private final NbtCompound resetNbt;

    public PlayerResetter(NbtCompound resetNbt) {
        this.resetNbt = resetNbt;
    }

    public void apply(ServerPlayerEntity player) {
        this.clearAttributeModifiers(player);
        player.clearStatusEffects();
        player.getCommandTags().clear();
        InventoryUtil.clear(player);
        player.readNbt(this.resetNbt);
    }

    private void clearAttributeModifiers(ServerPlayerEntity player) {
        var attributes = player.getAttributes();
        for (var attribute : Registries.ATTRIBUTE.getIndexedEntries()) {
            if (attributes.hasAttribute(attribute)) {
                var instance = attributes.getCustomInstance(attribute);
                if (instance != null) {
                    instance.clearModifiers();
                }
            }
        }
    }
}
