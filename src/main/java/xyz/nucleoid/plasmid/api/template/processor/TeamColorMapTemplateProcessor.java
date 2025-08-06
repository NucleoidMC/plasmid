package xyz.nucleoid.plasmid.api.template.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.attachment.PlasmidGameAttachments;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;
import xyz.nucleoid.plasmid.api.util.ColoredItems;

import java.util.HashMap;
import java.util.List;

/**
 * Template processor that recolors blocks and items found in block entities (by their ID) in a template with team-specific colors.
 *
 * <p><strong>This processor requires {@link PlasmidGameAttachments#TEAM_LIST} be attached to your activity before loading the processors.</strong>
 *
 * @param baseColors the colors to recolor. Each entry must correspond to a team of the loaded game.
 *
 * @see ColoredBlocks
 * @see ColoredItems
 *
 * @see ReplaceBlocksTemplateProcessor
 * @see ReplaceBlockEntitiesTemplateProcessor
 *
 * @author Hugman
 */
public record TeamColorMapTemplateProcessor(List<DyeColor> baseColors) implements MapTemplateProcessor {
    public static final MapCodec<TeamColorMapTemplateProcessor> CODEC = DyeColor.CODEC.listOf().fieldOf("base_colors").xmap(TeamColorMapTemplateProcessor::new, TeamColorMapTemplateProcessor::baseColors);

    @Override
    public MapTemplateProcessorType<?> getType() {
        return MapTemplateProcessorType.TEAM_COLORS;
    }

    @Override
    public void processTemplate(GameActivity activity, MapTemplate template) {
        var teamList = activity.getGameSpace().getAttachmentOrThrow(PlasmidGameAttachments.TEAM_LIST).list();

        if (teamList.size() > this.baseColors.size()) {
            throw new GameOpenException(Text.literal("Not enough base colors provided for the number of teams."));
        }

        var blockMap = new HashMap<Block, Block>();
        var blockEntityReplace = new HashMap<String, String>();
        for (int i = 0; i < teamList.size(); i++) {
            var baseColor = this.baseColors.get(i);
            var teamColor = teamList.get(i).config().blockDyeColor();
            blockMap.put(ColoredBlocks.wool(baseColor), ColoredBlocks.wool(teamColor));
            blockMap.put(ColoredBlocks.carpet(baseColor), ColoredBlocks.carpet(teamColor));
            blockMap.put(ColoredBlocks.terracotta(baseColor), ColoredBlocks.terracotta(teamColor));
            blockMap.put(ColoredBlocks.glazedTerracotta(baseColor), ColoredBlocks.glazedTerracotta(teamColor));
            blockMap.put(ColoredBlocks.concrete(baseColor), ColoredBlocks.concrete(teamColor));
            blockMap.put(ColoredBlocks.concretePowder(baseColor), ColoredBlocks.concretePowder(teamColor));
            blockMap.put(ColoredBlocks.glass(baseColor), ColoredBlocks.glass(teamColor));
            blockMap.put(ColoredBlocks.glassPane(baseColor), ColoredBlocks.glassPane(teamColor));
            blockMap.put(ColoredBlocks.bed(baseColor), ColoredBlocks.bed(teamColor));
            blockMap.put(ColoredBlocks.shulkerBox(baseColor), ColoredBlocks.shulkerBox(teamColor));
            blockMap.put(ColoredBlocks.candle(baseColor), ColoredBlocks.candle(teamColor));
            blockMap.put(ColoredBlocks.candleCake(baseColor), ColoredBlocks.candleCake(teamColor));
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.dye(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.dye(teamColor)).toString());
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.bundle(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.bundle(teamColor)).toString());
            blockEntityReplace.put(Registries.ITEM.getId(ColoredItems.harness(baseColor)).toString(), Registries.ITEM.getId(ColoredItems.harness(teamColor)).toString());
        }

        new ReplaceBlocksTemplateProcessor(blockMap).processTemplate(activity, template);

        for (var entry : blockMap.entrySet()) {
            blockEntityReplace.put(Registries.BLOCK.getId(entry.getKey()).toString(), Registries.BLOCK.getId(entry.getValue()).toString());
        }
        new ReplaceBlockEntitiesTemplateProcessor(blockEntityReplace).processTemplate(activity, template);
    }
}
