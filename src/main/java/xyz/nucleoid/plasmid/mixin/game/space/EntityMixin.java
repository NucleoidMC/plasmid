package xyz.nucleoid.plasmid.mixin.game.space;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
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
    @Shadow public abstract World getWorld();

    @Unique
    private boolean teleportIsolation = true;

    @Inject(method = "teleportTo", at = @At("HEAD"), cancellable = true)
    private void preventOutOfGameTeleports(TeleportTarget teleportTarget, CallbackInfoReturnable<Object> cir) {
        if (this.teleportIsolation && GameSpaceManager.get().byWorld(this.getWorld()) != GameSpaceManager.get().byWorld(teleportTarget.world())) {
            cir.setReturnValue(this);
        }
    }

    @Override
    public void plasmid$setTeleportIsolation(boolean value) {
        this.teleportIsolation = value;
    }
}
