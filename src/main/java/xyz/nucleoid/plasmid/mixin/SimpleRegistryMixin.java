package xyz.nucleoid.plasmid.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.PlasmidConfig;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Deprecated
@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin {
    @Shadow
    public abstract ResourceKey<? extends Registry<Object>> key();

    @Shadow
    @Final
    private Map<ResourceKey<Object>, Holder.Reference<Object>> byKey;

    @Shadow
    public abstract Holder.Reference<Object> register(ResourceKey<Object> key, Object value, RegistrationInfo info);

    @Inject(method = "freeze", at = @At("HEAD"))
    private void maybeRegisterInvalidConfigs(CallbackInfoReturnable<Registry<Object>> cir) {
        if (!PlasmidConfig.get().ignoreInvalidGames() || !this.key().equals(PlasmidRegistryKeys.GAME_CONFIG)) {
            return;
        }

        var keys = this.byKey.entrySet().stream().filter((entry) -> !entry.getValue().isBound()).toList();
        for (var key : keys) {
            Plasmid.LOGGER.error("Something depends on non-existing game config '{}'!", key.getKey().identifier());
            this.register(key.getKey(), new GameConfig<>(GameTypes.INVALID, null, null, null, null, CustomValuesConfig.empty(), key.getKey().identifier().toString()), RegistrationInfo.BUILT_IN);
        }
    }
}
