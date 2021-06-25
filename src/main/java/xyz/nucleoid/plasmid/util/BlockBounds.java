package xyz.nucleoid.plasmid.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Random;

public final class BlockBounds implements Iterable<BlockPos> {
    public static final Codec<BlockBounds> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                BlockPos.CODEC.fieldOf("min").forGetter(b -> b.min),
                BlockPos.CODEC.fieldOf("max").forGetter(b -> b.max)
        ).apply(instance, BlockBounds::new);
    });

    public static final BlockBounds EMPTY = BlockBounds.of(BlockPos.ORIGIN);

    private final BlockPos min;
    private final BlockPos max;

    public BlockBounds(BlockPos min, BlockPos max) {
        this.min = min(min, max);
        this.max = max(min, max);
    }

    public BlockBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public static BlockBounds of(BlockPos pos) {
        return new BlockBounds(pos, pos);
    }

    public static BlockBounds of(ChunkPos chunk) {
        return new BlockBounds(
                new BlockPos(chunk.getStartX(), 0, chunk.getStartZ()),
                new BlockPos(chunk.getEndX(), 255, chunk.getEndZ())
        );
    }

    public BlockBounds offset(BlockPos pos) {
        return new BlockBounds(
                this.min.add(pos),
                this.max.add(pos)
        );
    }

    public BlockBounds offset(int x, int y, int z) {
        return new BlockBounds(
                this.min.add(x, y, z),
                this.max.add(x, y, z)
        );
    }

    public boolean contains(BlockPos pos) {
        return this.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= this.min.getX() && y >= this.min.getY() && z >= this.min.getZ()
                && x <= this.max.getX() && y <= this.max.getY() && z <= this.max.getZ();
    }

    public boolean contains(int x, int z) {
        return x >= this.min.getX() && z >= this.min.getZ() && x <= this.max.getX() && z <= this.max.getZ();
    }

    public boolean intersects(BlockBounds bounds) {
        return this.max.getX() >= bounds.min.getX() && this.min.getX() <= bounds.max.getX()
                && this.max.getY() >= bounds.min.getY() && this.min.getY() <= bounds.max.getY()
                && this.max.getZ() >= bounds.min.getZ() && this.min.getZ() <= bounds.max.getZ();
    }

    @Nullable
    public BlockBounds intersection(BlockBounds bounds) {
        if (!this.intersects(bounds)) {
            return null;
        }

        BlockPos min = max(this.getMin(), bounds.getMin());
        BlockPos max = min(this.getMax(), bounds.getMax());
        return new BlockBounds(min, max);
    }

    public BlockBounds union(BlockBounds bounds) {
        BlockPos min = min(this.getMin(), bounds.getMin());
        BlockPos max = max(this.getMax(), bounds.getMax());
        return new BlockBounds(min, max);
    }

    public BlockPos getMin() {
        return this.min;
    }

    public BlockPos getMax() {
        return this.max;
    }

    public BlockPos getSize() {
        return this.max.subtract(this.min);
    }

    public Vec3d getCenter() {
        return new Vec3d(
                (this.min.getX() + this.max.getX() + 1) / 2.0,
                (this.min.getY() + this.max.getY() + 1) / 2.0,
                (this.min.getZ() + this.max.getZ() + 1) / 2.0
        );
    }

    public Vec3d getCenterBottom() {
        return new Vec3d(
                (this.min.getX() + this.max.getX() + 1) / 2.0,
                this.min.getY(),
                (this.min.getZ() + this.max.getZ() + 1) / 2.0
        );
    }

    public Vec3d getCenterTop() {
        return new Vec3d(
                (this.min.getX() + this.max.getX() + 1) / 2.0,
                this.max.getY() + 1.0,
                (this.min.getZ() + this.max.getZ() + 1) / 2.0
        );
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.iterate(this.min, this.max).iterator();
    }

    public LongSet asChunks() {
        int minChunkX = this.min.getX() >> 4;
        int minChunkZ = this.min.getZ() >> 4;
        int maxChunkX = this.max.getX() >> 4;
        int maxChunkZ = this.max.getZ() >> 4;

        LongOpenHashSet chunks = new LongOpenHashSet((maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1));

        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                chunks.add(ChunkPos.toLong(chunkX, chunkZ));
            }
        }

        return chunks;
    }

    public LongSet asChunkSections() {
        int minChunkX = this.min.getX() >> 4;
        int minChunkY = this.min.getY() >> 4;
        int minChunkZ = this.min.getZ() >> 4;
        int maxChunkX = this.max.getX() >> 4;
        int maxChunkY = this.max.getY() >> 4;
        int maxChunkZ = this.max.getZ() >> 4;

        LongOpenHashSet chunks = new LongOpenHashSet((maxChunkX - minChunkX + 1) * (maxChunkY - minChunkY + 1) * (maxChunkZ - minChunkZ + 1));

        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    chunks.add(ChunkSectionPos.asLong(chunkX, chunkY, chunkZ));
                }
            }
        }

        return chunks;
    }

    public BlockPos sampleBlock(Random random) {
        return new BlockPos(
                sampleAxis(random, this.min.getX(), this.max.getX()),
                sampleAxis(random, this.min.getY(), this.max.getY()),
                sampleAxis(random, this.min.getZ(), this.max.getZ())
        );
    }

    private static int sampleAxis(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public CompoundTag serialize(CompoundTag root) {
        root.putIntArray("min", new int[] { this.min.getX(), this.min.getY(), this.min.getZ() });
        root.putIntArray("max", new int[] { this.max.getX(), this.max.getY(), this.max.getZ() });
        return root;
    }

    public static BlockBounds deserialize(CompoundTag root) {
        int[] minArray = root.getIntArray("min");
        int[] maxArray = root.getIntArray("max");
        return new BlockBounds(
                new BlockPos(minArray[0], minArray[1], minArray[2]),
                new BlockPos(maxArray[0], maxArray[1], maxArray[2])
        );
    }

    public Box toBox() {
        return new Box(
                this.min.getX(), this.min.getY(), this.min.getZ(),
                this.max.getX() + 1.0, this.max.getY() + 1.0, this.max.getZ() + 1.0
        );
    }

    public static BlockPos min(BlockPos a, BlockPos b) {
        return new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
    }

    public static BlockPos max(BlockPos a, BlockPos b) {
        return new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
    }
}
