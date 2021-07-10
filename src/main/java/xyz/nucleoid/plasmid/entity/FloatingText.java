package xyz.nucleoid.plasmid.entity;

import net.minecraft.entity.Entity;
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
    private Vec3d position;
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

    // TODO: 0.5- migrate to patbox's floating text api
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
            Vec3d position = this.getPositionForLine(i, count);

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

    public void setPos(Vec3d pos) {
        if (pos.equals(this.position)) {
            return;
        }

        this.position = pos;

        ArmorStandEntity[] entities = this.entities;
        if (entities != null) {
            for (int i = 0; i < entities.length; i++) {
                ArmorStandEntity entity = entities[i];
                Vec3d linePos = this.getPositionForLine(i, entities.length);
                entity.setPos(linePos.x, linePos.y, linePos.z);
            }
        }
    }

    private Vec3d getPositionForLine(int line, int count) {
        double y;
        switch (this.verticalAlign) {
            case TOP:
                y = this.position.y - line * VERTICAL_SPACE;
                break;
            case BOTTOM:
                y = this.position.y + (count - 1 - line) * VERTICAL_SPACE;
                break;
            default:
            case CENTER:
                y = this.position.y + (count / 2.0 - line) * VERTICAL_SPACE;
                break;
        }

        return new Vec3d(this.position.x, y, this.position.z);
    }

    public void remove() {
        if (this.entities != null) {
            for (ArmorStandEntity entity : this.entities) {
                entity.remove(Entity.RemovalReason.DISCARDED);
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
