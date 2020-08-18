package xyz.nucleoid.plasmid.mixin.bubble;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorld;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    @Nullable
    public abstract ServerWorld getWorld(RegistryKey<World> key);

    @Redirect(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;sendToDimension(Lnet/minecraft/network/Packet;Lnet/minecraft/util/registry/RegistryKey;)V"
            )
    )
    private void sendWorldTimeUpdate(PlayerManager playerManager, Packet<?> packet, RegistryKey<World> dimension) {
        ServerWorld world = this.getWorld(dimension);

        BubbleWorld bubble = BubbleWorld.forWorld(world);
        if (bubble != null) {
            BubbleWorldConfig config = bubble.getConfig();
            if (config.hasTimeOfDay()) {
                playerManager.sendToDimension(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), false), dimension);
                return;
            }
        }

        playerManager.sendToDimension(packet, dimension);
    }
}
