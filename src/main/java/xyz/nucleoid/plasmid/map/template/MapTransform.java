package xyz.nucleoid.plasmid.map.template;

import net.minecraft.block.BlockState;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.util.BlockBounds;

public interface MapTransform {
    static MapTransform translation(int x, int y, int z) {
        return new MapTransform() {
            @Override
            public BlockPos.Mutable transformPoint(BlockPos.Mutable mutablePos) {
                return mutablePos.move(x, y, z);
            }

            @Override
            public Vec3d transformedPoint(Vec3d pos) {
                return pos.add(x, y, z);
            }
        };
    }

    static MapTransform rotationAround(BlockPos pivot, BlockRotation rotation, BlockMirror mirror) {
        return new MapTransform() {
            @Override
            public BlockPos.Mutable transformPoint(BlockPos.Mutable mutablePos) {
                BlockPos result = this.transformedPoint(mutablePos);
                mutablePos.set(result);
                return mutablePos;
            }

            @Override
            public BlockPos transformedPoint(BlockPos pos) {
                return Structure.transformAround(pos, mirror, rotation, pivot);
            }

            @Override
            public Vec3d transformedPoint(Vec3d pos) {
                return Structure.transformAround(pos, mirror, rotation, pivot);
            }

            @Override
            public BlockState transformedBlock(BlockState state) {
                return state.rotate(rotation).mirror(mirror);
            }
        };
    }

    BlockPos.Mutable transformPoint(BlockPos.Mutable mutablePos);

    default BlockPos transformedPoint(BlockPos pos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
        this.transformPoint(mutablePos);
        return mutablePos;
    }

    Vec3d transformedPoint(Vec3d pos);

    default BlockBounds transformedBounds(BlockBounds bounds) {
        return new BlockBounds(
                this.transformedPoint(bounds.getMin()),
                this.transformedPoint(bounds.getMax())
        );
    }

    default BlockState transformedBlock(BlockState state) {
        return state;
    }
}
