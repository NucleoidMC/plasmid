package xyz.nucleoid.plasmid.mixin.game.world;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.*;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.duck.ServerEntityManagerAccess;
import xyz.nucleoid.plasmid.duck.ThreadedAnvilChunkStorageAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageAccess {
    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;
    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
    @Shadow private boolean chunkHolderListDirty;
    @Shadow @Final LongSet unloadedChunks;
    @Shadow @Final private Queue<Runnable> unloadTaskQueue;
    @Shadow @Final private Long2ByteMap chunkToType;
    @Shadow @Final private Long2LongMap chunkToNextSaveTimeMs;
    @Shadow @Final private LongSet loadedChunks;
    @Shadow @Final private ServerLightingProvider lightingProvider;
    @Shadow @Final ServerWorld world;
    @Shadow @Final private ChunkTaskPrioritySystem chunkTaskPrioritySystem;
    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow private ChunkGenerator chunkGenerator;

    @Unique
    private NoiseConfig plasmid$noiseConfig;
    @Unique
    private StructurePlacementCalculator plasmid$structurePlacementCalculator;
    @Unique
    private final LongSet plasmid$chunksToDropData = new LongOpenHashSet();

    @Inject(method = "getUpdatedChunkNbt", at = @At("HEAD"), cancellable = true)
    private void loadChunkData(ChunkPos chunkPos, CallbackInfoReturnable<CompletableFuture<Optional<NbtCompound>>> cir) {
        if (this.plasmid$chunksToDropData.remove(chunkPos.toLong())) {
            cir.setReturnValue(CompletableFuture.completedFuture(Optional.empty()));
        }
    }

    @Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At("HEAD"))
    private void save(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
        if (chunk.needsSaving()) {
            this.plasmid$chunksToDropData.remove(chunk.getPos().toLong());
        }
    }

    @Inject(method = "getStructurePlacementCalculator", at = @At("HEAD"), cancellable = true)
    protected void getStructurePlacementCalculator(CallbackInfoReturnable<StructurePlacementCalculator> cir) {
        if (this.plasmid$structurePlacementCalculator != null) {
            cir.setReturnValue(this.plasmid$structurePlacementCalculator);
        }
    }

    @Inject(method = "getNoiseConfig", at = @At("HEAD"), cancellable = true)
    protected void getNoiseConfig(CallbackInfoReturnable<NoiseConfig> cir) {
        if (this.plasmid$noiseConfig != null) {
            cir.setReturnValue(this.plasmid$noiseConfig);
        }
    }

    @Override
    public void plasmid$setGenerator(ChunkGenerator generator) {
        this.chunkGenerator = generator;
        DynamicRegistryManager dynamicRegistryManager = this.world.getRegistryManager();
        long l = this.world.getSeed();
        if (generator instanceof NoiseChunkGenerator noiseChunkGenerator) {
            this.plasmid$noiseConfig = NoiseConfig.create(noiseChunkGenerator.getSettings().value(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        } else {
            this.plasmid$noiseConfig = NoiseConfig.create(ChunkGeneratorSettings.createMissingSettings(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        }

        this.plasmid$structurePlacementCalculator = generator.createStructurePlacementCalculator(dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), this.plasmid$noiseConfig, l);
    }

    @Override
    public void plasmid$clearChunks(LongSet chunksToDrop) {
        // Wait for any active generation work to complete
        for (ChunkHolder chunkHolder : this.currentChunkHolders.values()) {
            CompletableFuture<Chunk> savingFuture = chunkHolder.getSavingFuture();
            this.mainThreadExecutor.runTasks(savingFuture::isDone);
        }

        System.out.println();

        this.plasmid$chunksToDropData.addAll(chunksToDrop);

        Long2ByteMap oldLevels = new Long2ByteOpenHashMap();

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ChunkHolder chunkHolder : this.currentChunkHolders.values()) {
            ChunkPos pos = chunkHolder.getPos();
            ((ServerLightingProviderAccessor) this.lightingProvider).plasmid$updateChunkStatus(pos);
            futures.add(this.lightingProvider.enqueue(pos.x, pos.z));
            oldLevels.put(pos.toLong(), (byte) chunkHolder.getLevel());

            WorldChunk chunk = chunkHolder.getAccessibleChunk();
            if (chunk != null) {
                for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
                    chunk.removeBlockEntity(blockPos);
                }
            }
        }

        this.lightingProvider.tick();
        for (CompletableFuture<?> future : futures) {
            this.mainThreadExecutor.runTasks(future::isDone);
        }

        this.loadedChunks.clear();
        this.currentChunkHolders.clear();
        this.chunkHolders.clear();
        this.chunkHolderListDirty = true;
        this.unloadedChunks.clear();
        this.unloadTaskQueue.clear();
        this.chunkToType.clear();
        this.chunkToNextSaveTimeMs.clear();

        var toDelete = this.world.getEntitiesByType(
                TypeFilter.instanceOf(Entity.class),
                e -> !(e instanceof ServerPlayerEntity) && chunksToDrop.contains(e.getChunkPos().toLong())
        );

        for (var entity : toDelete) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }

        ((ServerEntityManagerAccess) this.world.entityManager).plasmid$clearChunks(chunksToDrop);

        for (Long2ByteMap.Entry entry : Long2ByteMaps.fastIterable(oldLevels)) {
            ChunkPos pos = new ChunkPos(entry.getLongKey());
            int level = entry.getByteValue();
            if (ChunkLevels.isAccessible(level)) {
                ChunkHolder holder = new ChunkHolder(pos, level, this.world, this.lightingProvider, this.chunkTaskPrioritySystem, (ThreadedAnvilChunkStorage) (Object) this);
                this.currentChunkHolders.put(pos.toLong(), holder);
            }
        }

        for (ChunkHolder chunkHolder : this.currentChunkHolders.values()) {
            ((ChunkHolderAccessor) chunkHolder).plasmid$updateFutures((ThreadedAnvilChunkStorage) (Object) this, this.mainThreadExecutor);
        }
    }
}
