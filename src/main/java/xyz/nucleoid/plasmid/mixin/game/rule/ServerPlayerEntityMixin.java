package xyz.nucleoid.plasmid.mixin.game.rule;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @WrapWithCondition(
            method = "tick",
            // The targeted call handles shifting to stop spectating
            // The other call handles dead entities, which should always stop being spectated
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setCameraEntity(Lnet/minecraft/entity/Entity;)V", ordinal = 0)
    )
    private boolean preventStopSpectatingEntity(ServerPlayerEntity player, Entity entity) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);

        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRuleType.STOP_SPECTATING_ENTITY) == EventResult.DENY) {
            return false;
        }

        return true;
    }

    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void allowPvPInGames(CallbackInfoReturnable<Boolean> cir) {
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(this);
        if (gameSpace != null) {
            cir.setReturnValue(true);
        }
    }
}
