package xyz.nucleoid.plasmid.mixin.fake;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.fake.Fake;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    private static final int DUMMY_ID = Integer.MIN_VALUE;

    @Redirect(
            method = "toTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I",
                    ordinal = 1
            )
    )
    private static <T> int getRawIdForEntry(Registry<T> registry, @Nullable T object) {
        T proxy = Fake.getProxy(object);
        if (proxy != object) {
            return DUMMY_ID;
        }

        return registry.getRawId(object);
    }

    @Redirect(
            method = "toTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                    ordinal = 0
            )
    )
    private static void putEntryOrSkip(CompoundTag tag, String key, int value) {
        if (value != DUMMY_ID) {
            tag.putInt(key, value);
        }
    }
}
