package xyz.nucleoid.plasmid.api.map.template.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class MapTemplateProcessorTypes {
    public static final MapCodec<ReplaceBlocksTemplateProcessor> REPLACE_BLOCKS = of("replace_blocks", ReplaceBlocksTemplateProcessor.CODEC);
    public static final MapCodec<ReplaceBlockEntitiesTemplateProcessor> REPLACE_BLOCK_ENTITIES = of("replace_block_entities", ReplaceBlockEntitiesTemplateProcessor.CODEC);
    public static final MapCodec<TeamColorMapTemplateProcessor> TEAM_COLORS = of("team_colors", TeamColorMapTemplateProcessor.CODEC);

    private static <T extends MapTemplateProcessor> MapCodec<T> of(String name, MapCodec<T> codec) {
        return of(Identifier.of(Plasmid.ID, name), codec);
    }

    public static <T extends MapTemplateProcessor> MapCodec<T> of(Identifier identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE, identifier, codec);
    }
}
