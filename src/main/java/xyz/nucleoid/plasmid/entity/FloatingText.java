package xyz.nucleoid.plasmid.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class FloatingText {
    private static final double VERTICAL_SPACE = 0.3;

    private final ServerWorld world;
    private final Vec3d position;
    private final VerticalAlign verticalAlign;

    private ArmorStandEntity[] entities;

    private FloatingText(ServerWorld world, Vec3d position, VerticalAlign verticalAlign) {
        this.world = world;
        this.position = position;
        this.verticalAlign = verticalAlign;
    }

    public static FloatingText create(ServerWorld world, Vec3d position, VerticalAlign align) {
        return new FloatingText(world, position, align);
    }

    public static FloatingText spawn(ServerWorld world, Vec3d position, Text text) {
        FloatingText floatingText = new FloatingText(world, position, VerticalAlign.CENTER);
        floatingText.setText(text);
        return floatingText;
    }

    public static FloatingText spawn(ServerWorld world, Vec3d position, Text[] lines, VerticalAlign verticalAlign) {
        FloatingText floatingText = new FloatingText(world, position, verticalAlign);
        floatingText.setText(lines);
        return floatingText;
    }

    public void setText(Text line) {
        this.setText(new Text[] { line });
    }

    public void setText(Text[] lines) {
        if (this.entities == null || this.entities.length != lines.length) {
            this.remove();
            this.entities = this.spawnEntities(lines.length);
        }

        for (int i = 0; i < lines.length; i++) {
            ArmorStandEntity entity = this.entities[i];
            entity.setCustomName(lines[i]);
        }
    }

    private ArmorStandEntity[] spawnEntities(int count) {
        ArmorStandEntity[] entities = new ArmorStandEntity[count];

        for (int i = 0; i < count; i++) {
            double y;
            switch (this.verticalAlign) {
                case TOP:
                    y = this.position.y - i * VERTICAL_SPACE;
                    break;
                case BOTTOM:
                    y = this.position.y + (count - 1 - i) * VERTICAL_SPACE;
                    break;
                default:
                case CENTER:
                    y = this.position.y + (i - count / 2.0) * VERTICAL_SPACE;
                    break;
            }

            Vec3d position = new Vec3d(this.position.x, y, this.position.z);

            ArmorStandEntity entity = EntityType.ARMOR_STAND.create(this.world, null, null, null, new BlockPos(position), SpawnReason.COMMAND, false, false);
            if (entity == null) {
                return null;
            }

            NonPersistentEntity.setNonPersistent(entity);

            entity.setPos(position.x, position.y, position.z);

            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setNoGravity(true);
            entity.setMarker(true);
            entity.setCustomNameVisible(true);

            if (!this.world.spawnEntity(entity)) {
                return null;
            }

            entities[i] = entity;
        }

        return entities;
    }

    public void remove() {
        if (this.entities != null) {
            for (ArmorStandEntity entity : this.entities) {
                entity.remove();
            }
            this.entities = null;
        }
    }

    public enum VerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }
}
