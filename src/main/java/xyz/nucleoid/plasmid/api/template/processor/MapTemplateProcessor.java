package xyz.nucleoid.plasmid.api.template.processor;

import com.mojang.serialization.Codec;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

/**
 * Modifies a {@link MapTemplate}. It must be used in the context of a {@link GameActivity}.
 *
 * @see MapTemplateProcessorType
 *
 * @author Hugman
 */
public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> TYPE_CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.getCodec().dispatch(MapTemplateProcessor::getType, MapTemplateProcessorType::codec);

    void processTemplate(GameActivity activity, MapTemplate template);

    MapTemplateProcessorType<?> getType();
}
