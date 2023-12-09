package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.BlockResourceCreator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.common.GameResourcePack;

import java.util.Optional;

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

    public static Optional<GameResourcePack> resourcePack;

    @Override
    public void onInitialize() {
        GameType.register(new Identifier(ID, "test"), TestConfig.CODEC, TestGame::open);
        GameType.register(new Identifier(ID, "no_join"), TestConfig.CODEC, PlayerlessGame::open);
        GameType.register(new Identifier(ID, "test_rp"), TestConfig.CODEC, TestGameWithResourcePack::open);
        GameType.register(new Identifier(ID, "jank"), TestConfig.CODEC, JankGame::open);
        Registry.register(Registries.BLOCK, id("test_block"), TEST_BLOCK);
        Registry.register(Registries.ITEM, id("test_item"), TEST_ITEM);


        CREATOR.addAssetSource("plasmid-test-mod");
        resourcePack = GameResourcePack.from(new Identifier(ID, "test"), CREATOR);

    }

    private static final Identifier id(String path) {
        return new Identifier(ID, path);
    }
}
