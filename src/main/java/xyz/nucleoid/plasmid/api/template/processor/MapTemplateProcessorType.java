package xyz.nucleoid.plasmid.api.template.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;

public record MapTemplateProcessorType<T extends MapTemplateProcessor>(MapCodec<T> codec) {
    public static final MapTemplateProcessorType<ReplaceBlocksTemplateProcessor> REPLACE_BLOCKS = of("replace_blocks", ReplaceBlocksTemplateProcessor.CODEC);
    public static final MapTemplateProcessorType<ReplaceBlockEntitiesTemplateProcessor> REPLACE_BLOCK_ENTITIES = of("replace_block_entities", ReplaceBlockEntitiesTemplateProcessor.CODEC);
    public static final MapTemplateProcessorType<TeamColorMapTemplateProcessor> TEAM_COLORS = of("team_colors", TeamColorMapTemplateProcessor.CODEC);

    private static <T extends MapTemplateProcessor> MapTemplateProcessorType<T> of(String name, MapCodec<T> codec) {
        return of(Identifier.of(Plasmid.ID, name), codec);
    }

    public static <T extends MapTemplateProcessor> MapTemplateProcessorType<T> of(Identifier identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE, identifier, new MapTemplateProcessorType<>(codec));
    }
}
