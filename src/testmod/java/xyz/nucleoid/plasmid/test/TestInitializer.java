package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.BlockResourceCreator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.GameResourcePack;

import java.util.Optional;

public class TestInitializer implements ModInitializer {
    public static final String ID = "testmod";
    public static final ResourcePackCreator CREATOR = ResourcePackCreator.create();
    public static final BlockResourceCreator BLOCK_CREATOR = BlockResourceCreator.of(CREATOR);

    private static final RegistryKey<Block> TEST_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("test_block"));

    public static final Block TEST_BLOCK = new TestBlock(
            AbstractBlock.Settings.copy(Blocks.STONE)
                    .registryKey(TEST_BLOCK_KEY),
            BLOCK_CREATOR.requestBlock(BlockModelType.TRANSPARENT_BLOCK, PolymerBlockModel.of(id("block/chair")))
    );

    private static final RegistryKey<Item> TEST_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, id("test_item"));

    public static final Item TEST_ITEM = new PolymerBlockItem(
            TEST_BLOCK,
            new Item.Settings().registryKey(TEST_ITEM_KEY)
    );

    public static Optional<GameResourcePack> resourcePack;

    @Override
    public void onInitialize() {
        GameType.register(Identifier.of(ID, "test"), TestConfig.CODEC, TestGame::open);
        GameType.register(Identifier.of(ID, "persistent"), MapCodec.unit(Unit.INSTANCE), PersistentGame::open);
        GameType.register(Identifier.of(ID, "no_join"), TestConfig.CODEC, PlayerlessGame::open);
        GameType.register(Identifier.of(ID, "test_rp"), TestConfig.CODEC, TestGameWithResourcePack::open);
        GameType.register(Identifier.of(ID, "jank"), TestConfig.CODEC, JankGame::open);
        Registry.register(Registries.BLOCK, TEST_BLOCK_KEY, TEST_BLOCK);
        Registry.register(Registries.ITEM, TEST_ITEM_KEY, TEST_ITEM);


        CREATOR.addAssetSource("plasmid-test-mod");
        resourcePack = GameResourcePack.from(Identifier.of(ID, "test"), CREATOR);

    }

    private static final Identifier id(String path) {
        return Identifier.of(ID, path);
    }
}
