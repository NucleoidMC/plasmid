package xyz.nucleoid.plasmid.mixin.fake;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.IdList;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.fake.FakedIdList;

import java.util.function.Function;

@Mixin(PalettedContainer.class)
public class PalettedContainerMixin<T> {
    @Shadow
    @Final
    @Mutable
    private IdList<T> idList;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Palette<T> fallbackPalette, IdList<T> idList, Function<CompoundTag, T> elementDeserializer, Function<T, CompoundTag> elementSerializer, T defaultElement, CallbackInfo ci) {
        this.idList = new FakedIdList<>(this.idList);
    }
}
