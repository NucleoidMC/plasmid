package xyz.nucleoid.plasmid.mixin.custom;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.entity.NonPersistentEntity;

@Mixin(Entity.class)
public abstract class EntityMixin implements NonPersistentEntity {
    private boolean nonPersistent;

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
}
