package xyz.nucleoid.plasmid.mixin.bubble;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorld;
import xyz.nucleoid.plasmid.world.bubble.CloseBubbleWorld;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldHolder;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements BubbleWorldHolder, CloseBubbleWorld {
    @Shadow
    @Final
    private ServerTickScheduler<Block> blockTickScheduler;
    @Shadow
    @Final
    private ServerTickScheduler<Fluid> fluidTickScheduler;

    @Shadow
    @Final
    private Queue<Entity> entitiesToLoad;
    @Shadow
    @Final
    private Int2ObjectMap<Entity> entitiesById;
    @Shadow
    @Final
    private Map<UUID, Entity> entitiesByUuid;
    @Shadow
    @Final
    private Set<EntityNavigation> entityNavigations;

    private BubbleWorld bubbleWorld;

    private ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimension, Supplier<Profiler> profiler, boolean client, boolean debug, long seed) {
        super(properties, registryKey, dimension, profiler, client, debug, seed);
    }

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void onPlayerAdded(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleWorld bubble = BubbleWorld.forWorld(this);
        if (bubble != null && !bubble.containsPlayer(player)) {
            bubble.kickPlayer(player);
        }
    }

    @Inject(method = "removePlayer", at = @At("RETURN"))
    private void onPlayerRemoved(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleWorld bubble = BubbleWorld.forWorld(this);
        if (bubble != null) {
            bubble.removePlayer(player);
        }
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void save(ProgressListener progressListener, boolean flush, boolean enabled, CallbackInfo ci) {
        if (flush && this.bubbleWorld != null) {
            ci.cancel();
        }
    }

    @Override
    public void setBubbleWorld(BubbleWorld bubbleWorld) {
        this.bubbleWorld = bubbleWorld;
    }

    @Nullable
    @Override
    public BubbleWorld getBubbleWorld() {
        return this.bubbleWorld;
    }

    @Override
    public void closeBubble() {
        CloseBubbleWorld.closeBubble(this.blockTickScheduler);
        CloseBubbleWorld.closeBubble(this.fluidTickScheduler);
        CloseBubbleWorld.closeBubble(this.getChunkManager());

        this.clearBlockEntities();
        this.clearEntities();
    }

    private void clearEntities() {
        this.entityNavigations.clear();

        this.entitiesToLoad.forEach(Entity::remove);
        this.entitiesById.values().forEach(Entity::remove);

        this.entitiesToLoad.clear();
        this.entitiesById.clear();
        this.entitiesByUuid.clear();
    }

    private void clearBlockEntities() {
        this.pendingBlockEntities.forEach(BlockEntity::markRemoved);
        this.blockEntities.forEach(BlockEntity::markRemoved);

        this.pendingBlockEntities.clear();
        this.blockEntities.clear();
        this.tickingBlockEntities.clear();
        this.unloadedBlockEntities.clear();
    }
}
