package xyz.nucleoid.plasmid.mixin.bubble;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.plasmid.world.bubble.CloseBubbleWorld;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements CloseBubbleWorld {
    @Shadow
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;
    @Shadow
    @Final
    private LongSet loadedChunks;
    @Shadow
    @Final
    private AtomicInteger totalChunksLoadedCount;
    @Shadow
    private boolean chunkHolderListDirty;
    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload;
    @Shadow
    @Final
    private Queue<Runnable> unloadTaskQueue;
    @Shadow
    @Final
    private Long2ByteMap chunkToType;

    @Shadow
    @Final
    private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Override
    public void closeBubble() {
        this.chunkHolders.clear();
        this.currentChunkHolders.clear();
        this.loadedChunks.clear();
        this.totalChunksLoadedCount.set(0);
        this.chunkHolderListDirty = true;
        this.chunksToUnload.clear();
        this.unloadTaskQueue.clear();
        this.chunkToType.clear();
        this.entityTrackers.clear();
    }
}
