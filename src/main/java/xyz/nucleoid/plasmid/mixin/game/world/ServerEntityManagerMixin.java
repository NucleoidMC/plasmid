package xyz.nucleoid.plasmid.mixin.game.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.ChunkDataList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.duck.ServerEntityManagerAccess;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin<T> implements ServerEntityManagerAccess {
    @Shadow @Final private Queue<ChunkDataList<T>> loadingQueue;
    @Unique
    private final LongSet plasmid$chunksToDropData = new LongOpenHashSet();

    @Override
    public void plasmid$clearChunks(LongSet chunksToDrop) {
        this.plasmid$chunksToDropData.addAll(chunksToDrop);
    }

    @Inject(method = "scheduleRead", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), cancellable = true)
    private void scheduleRead(long chunkPos, CallbackInfo ci) {
        if (this.plasmid$chunksToDropData.remove(chunkPos)) {
            this.loadingQueue.add(new ChunkDataList<>(new ChunkPos(chunkPos), List.of()));
            ci.cancel();
        }
    }

    @Inject(method = "trySave", at = @At("HEAD"))
    private void trySave(long chunkPos, Consumer<T> action, CallbackInfoReturnable<Boolean> cir) {
        this.plasmid$chunksToDropData.remove(chunkPos);
    }
}
