package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Template processor that replaces blocks in a template with specified blocks.
 *
 * @param blocks a map of blocks to replace, where the key is the block to be replaced and the value is the new block
 *
 * @author Hugman
 */
public record ReplaceBlocksTemplateProcessor(Map<Block, Block> blocks) implements MapTemplateProcessor {
    public static final MapCodec<ReplaceBlocksTemplateProcessor> CODEC = Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("blocks").xmap(ReplaceBlocksTemplateProcessor::new, ReplaceBlocksTemplateProcessor::blocks);

    @Override
    public MapCodec<? extends MapTemplateProcessor> getCodec() {
        return CODEC;
    }

    @Override
    public void processTemplate(MapTemplate template, ContextMap.Builder parameters) {
        template.getBounds().forEach(pos -> {
            var state = template.getBlockState(pos);
            var block = state.getBlock();
            Block newBlock = null;
            for (var entry : this.blocks.entrySet()) {
                if (entry.getKey() == block) {
                    newBlock = entry.getValue();
                    break;
                }
            }
            if (newBlock != null) {
                BlockState newState = newBlock.defaultBlockState();
                for (Property property : state.getProperties()) {
                    newState = newState.hasProperty(property) ? newState.setValue(property, state.getValue(property)) : newState;
                }
                template.setBlockState(pos, newState);
            }
        });
    }
}
