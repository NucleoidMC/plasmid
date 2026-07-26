package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.BlockResourceCreator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.MixinEnvironment;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.game.common.GameResourcePack;

import java.util.Optional;

public class TestInitializer implements ModInitializer {
    public static final String ID = "testmod";
    public static final ResourcePackCreator CREATOR = ResourcePackCreator.create();
    public static final BlockResourceCreator BLOCK_CREATOR = BlockResourceCreator.of(CREATOR);

    private static final ResourceKey<Block> TEST_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id("test_block"));

    public static final Block TEST_BLOCK = new TestBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .setId(TEST_BLOCK_KEY),
            BLOCK_CREATOR.requestBlock(BlockModelType.LEAVES, PolymerBlockModel.of(id("block/chair")))
    );

    private static final ResourceKey<Item> TEST_ITEM_KEY = ResourceKey.create(Registries.ITEM, id("test_item"));

    public static final Item TEST_ITEM = new PolymerBlockItem(
            TEST_BLOCK,
            new Item.Properties().setId(TEST_ITEM_KEY)
    );

    public static Optional<GameResourcePack> resourcePack;

    @Override
    public void onInitialize() {
        MixinEnvironment.getCurrentEnvironment().audit();

        GameTypes.register(Identifier.fromNamespaceAndPath(ID, "test"), TestConfig.CODEC, TestGame::open);
        GameTypes.register(Identifier.fromNamespaceAndPath(ID, "persistent"), MapCodec.unit(Unit.INSTANCE), PersistentGame::open);
        GameTypes.register(Identifier.fromNamespaceAndPath(ID, "no_join"), TestConfig.CODEC, PlayerlessGame::open);
        GameTypes.register(Identifier.fromNamespaceAndPath(ID, "test_rp"), TestConfig.CODEC, TestGameWithResourcePack::open);
        GameTypes.register(Identifier.fromNamespaceAndPath(ID, "jank"), TestConfig.CODEC, JankGame::open);
        Registry.register(BuiltInRegistries.BLOCK, TEST_BLOCK_KEY, TEST_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, TEST_ITEM_KEY, TEST_ITEM);


        CREATOR.addAssetSource("plasmid-test-mod");
        resourcePack = GameResourcePack.from(Identifier.fromNamespaceAndPath(ID, "test"), CREATOR);

    }

    private static final Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
