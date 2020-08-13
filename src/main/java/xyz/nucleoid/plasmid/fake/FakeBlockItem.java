package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import xyz.nucleoid.plasmid.fake.Fake;

public class FakeBlockItem extends BlockItem implements Fake<Item> {

    public final Block fakeBlock;

    public FakeBlockItem(Block fakeBlock, Settings settings) {
        super(fakeBlock, settings);
        this.fakeBlock = fakeBlock;
    }

    @Override
    public Item getFaking() {
        return fakeBlock.asItem();
    }
}
