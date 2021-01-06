package xyz.nucleoid.plasmid.mixin.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.entity.PlasmidPlayer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlasmidPlayer {

    @Shadow @Final public MinecraftServer server;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    private boolean usingPlasmidApi;

    @Override
    public boolean isUsingPlasmidApi() {
        return this.usingPlasmidApi;
    }

    @Override
    public void setUsingPlasmidApi(boolean usingPlasmidApi) {
        this.usingPlasmidApi = usingPlasmidApi;
    }
}
