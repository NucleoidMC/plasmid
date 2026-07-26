package xyz.nucleoid.plasmid.mixin.game.space;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.player.isolation.TeleportIsolated;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements TeleportIsolated {
    @Unique
    private boolean teleportIsolation = true;

    private ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), cancellable = true)
    private void preventOutOfGameTeleports(TeleportTransition teleportTarget, CallbackInfoReturnable<Object> cir) {
        if (this.teleportIsolation && GameSpaceManager.get().byPlayer(this) != GameSpaceManager.get().byLevel(teleportTarget.newLevel())) {
            cir.setReturnValue(this);
        }
    }

    @Override
    public void plasmid$setTeleportIsolation(boolean value) {
        this.teleportIsolation = value;
    }
}
