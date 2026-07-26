package xyz.nucleoid.plasmid.mixin.game.space;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.player.isolation.TeleportIsolated;

@Mixin(Entity.class)
public abstract class EntityMixin implements TeleportIsolated {
    @Shadow public abstract Level level();

    @Unique
    private boolean teleportIsolation = true;

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void preventOutOfGameTeleports(TeleportTransition teleportTarget, CallbackInfoReturnable<Object> cir) {
        if (this.teleportIsolation && GameSpaceManager.get().byLevel(this.level()) != GameSpaceManager.get().byLevel(teleportTarget.newLevel())) {
            cir.setReturnValue(this);
        }
    }

    @Override
    public void plasmid$setTeleportIsolation(boolean value) {
        this.teleportIsolation = value;
    }
}
