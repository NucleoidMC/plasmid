package net.gegy1000.plasmid.entity;

import com.google.common.base.Preconditions;
import net.gegy1000.plasmid.registry.TinyRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class CustomEntity {
    private static final TinyRegistry<CustomEntity> REGISTRY = TinyRegistry.newStable();

    private final Identifier id;
    private final Text name;

    private Interact interact;

    private CustomEntity(Identifier identifier, Text name) {
        this.id = identifier;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Identifier getIdentifier() {
        return this.id;
    }

    public Text getName() {
        return this.name;
    }

    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (this.interact == null) {
            return ActionResult.PASS;
        }
        return this.interact.interact(player, world, hand, entity, hitResult);
    }

    @Nullable
    public static CustomEntity get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    public void applyTo(Entity entity) {
        if (entity instanceof CustomizableEntity) {
            ((CustomizableEntity) entity).setCustomEntity(this);
        }
    }

    @Nullable
    public static CustomEntity match(Entity entity) {
        if (entity instanceof CustomizableEntity) {
            return ((CustomizableEntity) entity).getCustomEntity();
        }
        return null;
    }

    public static class Builder {
        private Identifier id;
        private Text name;
        private Interact interact;

        private Builder() {
        }

        public Builder id(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder name(Text name) {
            this.name = name;
            return this;
        }

        public Builder interact(Interact interact) {
            this.interact = interact;
            return this;
        }

        public CustomEntity register() {
            Preconditions.checkNotNull(this.id, "id not set");
            if (REGISTRY.containsKey(this.id)) {
                throw new IllegalArgumentException(this.id + " already registered");
            }

            CustomEntity item = new CustomEntity(this.id, this.name);
            item.interact = this.interact;

            REGISTRY.register(this.id, item);

            return item;
        }
    }

    public interface Interact {
        ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult);
    }
}
