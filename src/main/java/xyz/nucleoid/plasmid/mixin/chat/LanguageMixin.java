package xyz.nucleoid.plasmid.mixin.chat;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.chat.translation.ExtendedLanguage;

@Mixin(Language.class)
public class LanguageMixin {

    private static ImmutableMap<String, String> vanilla;

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false, ordinal = 0))
    private static ImmutableMap<String, String> getVanillaMap(ImmutableMap.Builder<String, String> map) {
        vanilla = map.build();
        return vanilla;
    }

    @Inject(method = "create", at = @At(value = "RETURN"), cancellable = true)
    private static void replaceLanguage(CallbackInfoReturnable<Language> cir) {
        cir.setReturnValue(new ExtendedLanguage(vanilla));
    }
}
