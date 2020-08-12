package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.fake.Fake;

@Mixin(IdList.class)
public class IdListMixin<T> {

    @ModifyVariable(method = "getRawId", at = @At("HEAD"), argsOnly = true, index = 1)
    private T modify(T entry) {
        return Fake.getProxy(entry);
    }
}
