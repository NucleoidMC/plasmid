package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.context.ContextParameterMap;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Map;

/**
 * Template processor that replaces blocks in a template with specified blocks.
 *
 * @param blocks a map of blocks to replace, where the key is the block to be replaced and the value is the new block
 *
 * @author Hugman
 */
public record ReplaceBlocksTemplateProcessor(Map<Block, Block> blocks) implements MapTemplateProcessor {
    public static final MapCodec<ReplaceBlocksTemplateProcessor> CODEC = Codec.unboundedMap(Registries.BLOCK.getCodec(), Registries.BLOCK.getCodec()).fieldOf("blocks").xmap(ReplaceBlocksTemplateProcessor::new, ReplaceBlocksTemplateProcessor::blocks);

    @Override
    public MapCodec<? extends MapTemplateProcessor> getCodec() {
        return CODEC;
    }

    @Override
    public void processTemplate(MapTemplate template, ContextParameterMap.Builder parameters) {
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
                BlockState newState = newBlock.getDefaultState();
                for (Property property : state.getProperties()) {
                    newState = newState.contains(property) ? newState.with(property, state.get(property)) : newState;
                }
                template.setBlockState(pos, newState);
            }
        });
    }
}
