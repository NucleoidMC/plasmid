package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import xyz.nucleoid.plasmid.fake.Fake;

public class FakeBlockItem extends BlockItem implements Fake<Item> {

    private final Block clientSideBlock;

    public FakeBlockItem(Block serverSideBlock, Block clientSideBlock, Settings settings) {
        super(serverSideBlock, settings);
        this.clientSideBlock = clientSideBlock;
    }

    @Override
    public Item getFaking() {
        return clientSideBlock.asItem();
    }
}
