package xyz.nucleoid.plasmid.mixin.game.world;

import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerLightingProvider.class)
public interface ServerLightingProviderAccessor {
    @Invoker("updateChunkStatus")
    void plasmid$updateChunkStatus(ChunkPos pos);
}
