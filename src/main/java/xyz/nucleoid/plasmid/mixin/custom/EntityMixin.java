package xyz.nucleoid.plasmid.mixin.custom;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.entity.CustomEntity;
import xyz.nucleoid.plasmid.entity.CustomizableEntity;
import xyz.nucleoid.plasmid.entity.NonPersistentEntity;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin implements CustomizableEntity, NonPersistentEntity {
    @Shadow
    public abstract void setCustomName(@Nullable Text name);

    @Shadow
    public World world;

    private CustomEntity customEntity;
    private boolean nonPersistent;

    @Override
    public void setCustomEntity(CustomEntity customEntity) {
        this.customEntity = customEntity;
        if (customEntity != null) {
            this.setCustomName(customEntity.getName());
        }
    }

    @Nullable
    @Override
    public CustomEntity getCustomEntity() {
        return this.customEntity;
    }

    @Override
    public void setNonPersistent() {
        this.nonPersistent = true;
    }

    @Inject(method = "saveToTag", at = @At("HEAD"), cancellable = true)
    private void saveToTag(CompoundTag tag, CallbackInfoReturnable<Boolean> ci) {
        if (this.nonPersistent) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfoReturnable<CompoundTag> ci) {
        if (this.customEntity != null) {
            root.putString("custom_entity", this.customEntity.getIdentifier().toString());
        }
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag root, CallbackInfo ci) {
        if (root.contains("custom_entity")) {
            Identifier customId = new Identifier(root.getString("custom_entity"));
            this.customEntity = CustomEntity.get(customId);
        }
    }
}
