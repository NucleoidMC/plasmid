package net.gegy1000.plasmid.mixin.map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.gegy1000.plasmid.game.world.ClearChunkTickets;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ChunkTicketManager.class)
public abstract class ChunkTicketManagerMixin implements ClearChunkTickets {
    @Shadow
    @Final
    private Set<ChunkHolder> chunkHolders;

    @Shadow
    @Final
    private LongSet chunkPositions;

    @Shadow
    @Final
    private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;

    @Shadow
    @Final
    private ChunkTicketManager.TicketDistanceLevelPropagator distanceFromTicketTracker;

    @Shadow
    public abstract boolean tick(ThreadedAnvilChunkStorage chunkStorage);

    @Override
    public void clearChunkTickets(ThreadedAnvilChunkStorage tacs) {
        this.chunkHolders.clear();
        this.chunkPositions.clear();
        this.playersByChunkPos.clear();

        ChunkPosDistanceLevelPropagator propagator = this.distanceFromTicketTracker;

        ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>>> iterator = Long2ObjectMaps.fastIterator(this.ticketsByPosition);
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>> entry = iterator.next();
            propagator.updateLevel(entry.getLongKey(), ThreadedAnvilChunkStorage.MAX_LEVEL + 1, false);

            iterator.remove();
        }

        this.tick(tacs);
    }
}
