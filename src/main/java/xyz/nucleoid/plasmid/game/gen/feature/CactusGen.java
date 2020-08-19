package xyz.nucleoid.plasmid.game.gen.feature;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import xyz.nucleoid.plasmid.game.gen.GenHelper;
import xyz.nucleoid.plasmid.game.gen.MapGen;

import java.util.Random;

public final class CactusGen implements MapGen {
    public static final CactusGen INSTANCE = new CactusGen(16, 8, 8);
    private final int count;
    private final int horizontalSpread;
    private final int verticalSpread;

    public CactusGen(int count, int horizontalSpread, int verticalSpread) {

        this.count = count;
        this.horizontalSpread = horizontalSpread;
        this.verticalSpread = verticalSpread;
    }

    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
        for(int i = 0; i < this.count; ++i) {
            int aX = random.nextInt(this.horizontalSpread) - random.nextInt(this.horizontalSpread);
            int aY = random.nextInt(this.verticalSpread) - random.nextInt(this.verticalSpread);
            int aZ = random.nextInt(this.horizontalSpread) - random.nextInt(this.horizontalSpread);
            BlockPos local = pos.add(aX, aY, aZ);

            boolean canGenerate = true;
            for (Direction dir : GenHelper.HORIZONTALS) {
                if (!world.getBlockState(local.offset(dir)).isAir()) {
                    canGenerate = false;
                    break;
                }
            }

            if (canGenerate && (world.getBlockState(local.down()) == Blocks.SAND.getDefaultState() || world.getBlockState(local.down()) == Blocks.CACTUS.getDefaultState()) && world.getBlockState(local).isAir()) {
                world.setBlockState(local, Blocks.CACTUS.getDefaultState(), 3);
            }
        }
    }
}