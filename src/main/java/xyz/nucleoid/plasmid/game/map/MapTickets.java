package xyz.nucleoid.plasmid.game.map;

import xyz.nucleoid.plasmid.util.BlockBounds;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.stream.Stream;

public final class MapTickets {
    private final ChunkPos minChunk;
    private final ChunkPos maxChunk;

    private MapTickets(ChunkPos minChunk, ChunkPos maxChunk) {
        this.minChunk = minChunk;
        this.maxChunk = maxChunk;
    }

    public static MapTickets acquire(ServerWorld world, BlockBounds bounds) {
        ChunkPos minChunk = new ChunkPos(bounds.getMin());
        ChunkPos maxChunk = new ChunkPos(bounds.getMax());

        MapTickets tickets = new MapTickets(minChunk, maxChunk);
        tickets.acquire(world);

        return tickets;
    }

    private void acquire(ServerWorld world) {
        ServerChunkManager chunkManager = world.getChunkManager();
        this.stream().forEach(ticketPos -> {
            chunkManager.setChunkForced(ticketPos, true);
            world.getChunk(ticketPos.x, ticketPos.z);
        });
    }

    public void release(ServerWorld world) {
        ServerChunkManager chunkManager = world.getChunkManager();
        this.stream().forEach(ticketPos -> {
            chunkManager.setChunkForced(ticketPos, false);
        });
    }

    private Stream<ChunkPos> stream() {
        return ChunkPos.stream(this.minChunk, this.maxChunk);
    }
}
