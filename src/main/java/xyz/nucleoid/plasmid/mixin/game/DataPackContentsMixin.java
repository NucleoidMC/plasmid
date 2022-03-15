package xyz.nucleoid.plasmid.mixin.game;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private static void captureRegistryManager(ResourceManager manager, DynamicRegistryManager.Immutable registryManager, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<DataPackContents>> ci) {
        GameConfigs.registryManager = registryManager;
    }
}
