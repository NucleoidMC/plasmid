package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import net.minecraft.util.context.ContextParameterMap;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

/**
 * Modifies a {@link MapTemplate}. It must be used in the context of a {@link GameActivity}.
 *
 * @author Hugman
 * @see MapTemplateProcessorType
 */
public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> TYPE_CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.getCodec().dispatch(MapTemplateProcessor::getType, MapTemplateProcessorType::codec);

    void processTemplate(MapTemplate template, ContextParameterMap.Builder parameters);

    MapTemplateProcessorType<?> getType();
}
