package xyz.nucleoid.plasmid.mixin.game.world;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.Executor;

@Mixin(ChunkHolder.class)
public interface ChunkHolderAccessor {
    @Invoker("updateFutures")
    void plasmid$updateFutures(ThreadedAnvilChunkStorage chunkStorage, Executor executor);
}
