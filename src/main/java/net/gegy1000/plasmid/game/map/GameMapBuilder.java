package net.gegy1000.plasmid.game.map;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public final class GameMapBuilder {
    private final ServerWorld world;
    private final BlockPos origin;

    private final BlockBounds bounds;
    private final MapTickets tickets;

    private final ImmutableList.Builder<GameRegion> regions = ImmutableList.builder();
    private final LongSet standardBlocks = new LongOpenHashSet();

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    private GameMapBuilder(ServerWorld world, BlockPos origin, BlockBounds bounds, MapTickets tickets) {
        this.world = world;
        this.origin = origin;
        this.bounds = bounds;
        this.tickets = tickets;
    }

    public static GameMapBuilder open(ServerWorld world, BlockPos origin, BlockBounds bounds) {
        MapTickets tickets = MapTickets.acquire(world, bounds.offset(origin));
        return new GameMapBuilder(world, origin, bounds, tickets);
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        setBlockState(pos, state, true);
    }

    public void setBlockState(BlockPos pos, BlockState state, boolean addToStandardBlocks) {
        BlockPos globalPos = this.localToGlobal(pos);

        this.world.setBlockState(globalPos, state, GameMap.SET_BLOCK_FLAGS);

        long key = globalPos.asLong();
        if (!state.isAir() && addToStandardBlocks) {
            this.standardBlocks.add(key);
        } else {
            this.standardBlocks.remove(key);
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.world.getBlockState(this.localToGlobal(pos));
    }

    public int getTopY(Heightmap.Type heightmap, BlockPos pos) {
        int x = pos.getX() + this.origin.getX();
        int z = pos.getZ() + this.origin.getZ();
        return this.world.getTopY(heightmap, x, z) - this.origin.getY();
    }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
        BlockPos globalPos = this.localToGlobal(pos);
        blockEntity.setLocation(this.world, globalPos);

        this.world.setBlockEntity(globalPos, blockEntity);
    }

    public void addRegion(String marker, BlockBounds bounds) {
        bounds = this.localToGlobal(bounds);
        this.regions.add(new GameRegion(marker, bounds));
    }

    public void addRegion(GameRegion region) {
        this.addRegion(region.getMarker(), region.getBounds());
    }

    private BlockPos localToGlobal(BlockPos pos) {
        return this.mutablePos.set(
                this.origin.getX() + pos.getX(),
                this.origin.getY() + pos.getY(),
                this.origin.getZ() + pos.getZ()
        );
    }

    private BlockBounds localToGlobal(BlockBounds bounds) {
        return new BlockBounds(
                this.localToGlobal(bounds.getMin()).toImmutable(),
                this.localToGlobal(bounds.getMax()).toImmutable()
        );
    }

    public GameMap build() {
        ImmutableList<GameRegion> regions = this.regions.build();
        BlockBounds bounds = this.localToGlobal(this.bounds);
        return new GameMap(this.world, this.tickets, bounds, regions, this.standardBlocks);
    }
}
