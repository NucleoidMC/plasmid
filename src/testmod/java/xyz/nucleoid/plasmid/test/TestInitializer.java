package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import eu.pb4.polymer.ext.blocks.api.BlockResourceCreator;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

public class TestInitializer implements ModInitializer {
    public static final String ID = "testmod";
    public static final ResourcePackCreator CREATOR = ResourcePackCreator.create();
    public static final BlockResourceCreator BLOCK_CREATOR = BlockResourceCreator.of(CREATOR);

    public static final Block TEST_BLOCK = new TestBlock(
            AbstractBlock.Settings.copy(Blocks.STONE),
            BLOCK_CREATOR.requestBlock(BlockModelType.TRANSPARENT_BLOCK, PolymerBlockModel.of(id("block/chair")))
    );

    public static final Item TEST_ITEM = new TestItem(
            TEST_BLOCK,
            new Item.Settings(),
            CREATOR.requestModel(Items.NOTE_BLOCK, id("block/chair"))
    );

    public static GameResourcePack RESOURCE_PACK = GameResourcePack.generate(id("pack"), CREATOR);

    @Override
    public void onInitialize() {
        GameType.register(new Identifier(ID, "test"), Codec.unit(Unit.INSTANCE), TestGame::open);
        GameType.register(new Identifier(ID, "test_rp"), Codec.unit(Unit.INSTANCE), TestGameWithResourcePack::open);
        Registry.register(Registry.BLOCK, id("test_block"), TEST_BLOCK);
        Registry.register(Registry.ITEM, id("test_item"), TEST_ITEM);


        CREATOR.addAssetSource("plasmid-test-mod");
        RESOURCE_PACK = GameResourcePack.generate(id("pack"), CREATOR);
    }

    private static final Identifier id(String path) {
        return new Identifier(ID, path);
    }
}
