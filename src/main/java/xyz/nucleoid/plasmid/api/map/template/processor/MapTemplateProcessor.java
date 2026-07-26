package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.function.Function;
import net.minecraft.util.context.ContextMap;

/**
 * Modifies a {@link MapTemplate}. It must be used in the context of a {@link GameActivity}.
 *
 * @author Hugman
 * @see MapTemplateProcessorTypes
 */
public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.byNameCodec().dispatch(MapTemplateProcessor::getCodec, Function.identity());

    void processTemplate(MapTemplate template, ContextMap.Builder parameters);

    MapCodec<? extends MapTemplateProcessor> getCodec();
}
