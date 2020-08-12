package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.fake.Fake;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> {

    @ModifyVariable(method = "getRawId", at = @At("HEAD"), argsOnly = true, index = 1)
    private T modify(T t) {
        if (t instanceof Fake) {
            return (T) ((Fake<?>) t).getFaking();
        } else {
            return t;
        }
    }
}
