package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

@Deprecated
public class FakeBlockItem extends BlockItem implements FakeItem {

    private final Block clientSideBlock;

    public FakeBlockItem(Block serverSideBlock, Block clientSideBlock, Settings settings) {
        super(serverSideBlock, settings);
        this.clientSideBlock = clientSideBlock;
    }

    @Override
    public Item asProxy() {
        return this.clientSideBlock.asItem();
    }
}
