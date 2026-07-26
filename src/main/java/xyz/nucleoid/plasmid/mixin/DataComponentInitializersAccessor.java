package xyz.nucleoid.plasmid.mixin;

import net.minecraft.core.component.DataComponentInitializers;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.core.component.DataComponentInitializers.class)
public interface DataComponentInitializersAccessor {
    @Accessor
    List<DataComponentInitializers.InitializerEntry<?>> getInitializers();
}
