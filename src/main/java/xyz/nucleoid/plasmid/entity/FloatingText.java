package xyz.nucleoid.plasmid.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public final class FloatingText {
    private final ArmorStandEntity entity;

    private FloatingText(ArmorStandEntity entity) {
        this.entity = entity;
    }

    @Nullable
    public static FloatingText spawn(ServerWorld world, Vec3d position, Text text) {
        ArmorStandEntity entity = EntityType.ARMOR_STAND.create(world, null, null, null, new BlockPos(position), SpawnReason.COMMAND, false, false);
        if (entity == null) {
            return null;
        }

        entity.setPos(position.x, position.y, position.z);

        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setMarker(true);

        entity.setCustomName(text);
        entity.setCustomNameVisible(true);

        if (!world.spawnEntity(entity)) {
            return null;
        }

        return new FloatingText(entity);
    }

    public void setText(Text text) {
        this.entity.setCustomName(text);
    }

    public void remove() {
        this.entity.remove();
    }
}
