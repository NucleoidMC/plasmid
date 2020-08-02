package net.gegy1000.plasmid.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class BlockBounds {
    public static final BlockBounds EMPTY = new BlockBounds(BlockPos.ORIGIN, BlockPos.ORIGIN);

    private final BlockPos min;
    private final BlockPos max;

    public BlockBounds(BlockPos min, BlockPos max) {
        this.min = min(min, max);
        this.max = max(min, max);
    }

    public BlockBounds(BlockPos pos) {
        this(pos, pos);
    }

    public BlockBounds offset(BlockPos pos) {
        return new BlockBounds(
                this.min.add(pos),
                this.max.add(pos)
        );
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= this.min.getX() && pos.getY() >= this.min.getY() && pos.getZ() >= this.min.getZ()
                && pos.getX() <= this.max.getX() && pos.getY() <= this.max.getY() && pos.getZ() <= this.max.getZ();
    }

    public boolean intersects(BlockBounds bounds) {
        return this.max.getX() >= bounds.min.getX() && this.min.getX() <= bounds.max.getX()
                && this.max.getZ() >= bounds.min.getY() && this.min.getX() <= bounds.max.getY()
                && this.max.getZ() >= bounds.min.getZ() && this.min.getX() <= bounds.max.getZ();
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
                (this.min.getX() + this.max.getX()) / 2.0 + 0.5,
                (this.min.getY() + this.max.getY()) / 2.0 + 0.5,
                (this.min.getZ() + this.max.getZ()) / 2.0 + 0.5
        );
    }

    public Iterable<BlockPos> iterate() {
        return BlockPos.iterate(this.min, this.max);
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
