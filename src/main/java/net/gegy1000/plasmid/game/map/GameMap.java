package net.gegy1000.plasmid.game.map;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class GameMap {
    static final int SET_BLOCK_FLAGS = 0b0110011;
    static final BlockState AIR = Blocks.AIR.getDefaultState();

    private final ServerWorld world;
    private final BlockBounds bounds;

    final MapTickets tickets;

    private final ImmutableList<GameRegion> regions;

    // TODO: move out of standard GameMap?
    private final LongSet protectedBlocks;

    GameMap(
            ServerWorld world, MapTickets tickets,
            BlockBounds bounds,
            ImmutableList<GameRegion> regions,
            LongSet protectedBlocks
    ) {
        this.world = world;
        this.tickets = tickets;
        this.bounds = bounds;
        this.regions = regions;
        this.protectedBlocks = protectedBlocks;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public List<GameRegion> getRegions() {
        return this.regions;
    }

    public Stream<BlockBounds> getRegions(String key) {
        return this.regions.stream()
                .filter(region -> region.getMarker().equals(key))
                .map(GameRegion::getBounds);
    }

    @Nullable
    public BlockBounds getFirstRegion(String key) {
        return this.getRegions(key).findFirst().orElse(null);
    }

    public boolean isProtectedBlock(BlockPos pos) {
        return this.protectedBlocks.contains(pos.asLong());
    }

    // TODO: we should rather have some system to totally revert the modified chunks
    public CompletableFuture<Void> delete() {
        MinecraftServer server = this.world.getServer();

        return server.submit(this::removeEntities)
                .thenRunAsync(this::clearBlocks, server)
                .thenRunAsync(this::releaseTickets, server);
    }

    private void removeEntities() {
        Box box = this.bounds.toBox();

        List<Entity> entities = this.world.getEntities(null, box);
        for (Entity entity : entities) {
            if (entity instanceof PlayerEntity) {
                continue;
            }

            entity.remove();
        }
    }

    private void clearBlocks() {
        this.bounds.iterate().forEach(pos -> {
            BlockEntity entity = this.world.getBlockEntity(pos);
            if (entity instanceof LootableContainerBlockEntity) {
                // clear block entity inventory so that it doesn't drop items
                ((LootableContainerBlockEntity) entity).clear();
            }

            this.world.setBlockState(pos, AIR, SET_BLOCK_FLAGS);
        });
    }

    private void releaseTickets() {
        this.tickets.release(this.world);
    }
}
