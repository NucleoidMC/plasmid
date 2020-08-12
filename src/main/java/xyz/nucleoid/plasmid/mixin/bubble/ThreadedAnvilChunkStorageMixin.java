package xyz.nucleoid.plasmid.mixin.bubble;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleChunkControl;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements BubbleChunkControl {
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
    private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract void method_27054(ChunkPos chunkPos);

    private LongSet createdChunks;

    private void clearChunks() {
        this.chunkHolders.clear();
        this.currentChunkHolders.clear();
        this.loadedChunks.clear();
        this.totalChunksLoadedCount.set(0);
        this.chunkHolderListDirty = true;
        this.chunksToUnload.clear();
        this.unloadTaskQueue.clear();
        this.chunkToType.clear();
    }

    @Override
    public void enable() {
        this.createdChunks = new LongOpenHashSet();
        this.clearChunks();
    }

    @Override
    public void disable() {
        this.clearChunks();
        this.createdChunks = null;
    }

    @Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true)
    private void loadChunk(ChunkPos pos, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> ci) {
        LongSet loadableChunks = this.createdChunks;
        if (loadableChunks == null) {
            return;
        }

        if (this.createdChunks.add(pos.toLong())) {
            ci.setReturnValue(CompletableFuture.supplyAsync(() -> {
                Chunk chunk = this.createUnloadedChunk(pos);
                return Either.left(chunk);
            }, this.mainThreadExecutor));
        }
    }

    private Chunk createUnloadedChunk(ChunkPos pos) {
        this.method_27054(pos);
        return new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA);
    }
}
