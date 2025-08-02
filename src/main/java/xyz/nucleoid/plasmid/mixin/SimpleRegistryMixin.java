package xyz.nucleoid.plasmid.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
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

@Deprecated
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin {
    @Shadow
    public abstract RegistryKey<? extends Registry<Object>> getKey();

    @Shadow
    @Final
    private Map<RegistryKey<Object>, RegistryEntry.Reference<Object>> keyToEntry;

    @Shadow
    public abstract RegistryEntry.Reference<Object> add(RegistryKey<Object> key, Object value, RegistryEntryInfo info);

    @Inject(method = "freeze", at = @At("HEAD"))
    private void maybeRegisterInvalidConfigs(CallbackInfoReturnable<Registry<Object>> cir) {
        if (!PlasmidConfig.get().ignoreInvalidGames() || !this.getKey().equals(PlasmidRegistryKeys.GAME_CONFIG)) {
            return;
        }

        var keys = this.keyToEntry.entrySet().stream().filter((entry) -> !entry.getValue().hasKeyAndValue()).toList();
        for (var key : keys) {
            Plasmid.LOGGER.error("Something depends on non-existing game config '{}'!", key.getKey().getValue());
            this.add(key.getKey(), new GameConfig<>(GameTypes.INVALID, null, null, null, null, CustomValuesConfig.empty(), key.getKey().getValue().toString()), RegistryEntryInfo.DEFAULT);
        }
    }
}
