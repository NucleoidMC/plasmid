package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.context.ContextParameterMap;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.function.Function;

/**
 * Modifies a {@link MapTemplate}. It must be used in the context of a {@link GameActivity}.
 *
 * @author Hugman
 * @see MapTemplateProcessorTypes
 */
public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.getCodec().dispatch(MapTemplateProcessor::getCodec, Function.identity());

    void processTemplate(MapTemplate template, ContextParameterMap.Builder parameters);

    MapCodec<? extends MapTemplateProcessor> getCodec();
}
