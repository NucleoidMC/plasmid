package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.fantasy.BubbleWorldSpawner;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public final class TestGame {
    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        MapTemplate template = TestGame.buildTemplate();

        ChunkGenerator generator = new TemplateChunkGenerator(context.getServer(), template, BlockPos.ORIGIN);

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(generator)
                .setSpawner(BubbleWorldSpawner.atSurface(BlockPos.ORIGIN))
                .setDefaultGameMode(GameMode.ADVENTURE)
                .setTimeOfDay(6000)
                .setGameRule(GameRules.DO_MOB_SPAWNING, false)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false);

        return context.createOpenProcedure(worldConfig, game -> {
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

            game.on(PlayerDeathListener.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });
        });
    }

    private static MapTemplate buildTemplate() {
        MapTemplate template = MapTemplate.createEmpty();

        for (BlockPos pos : BlockPos.iterate(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, Blocks.BLUE_STAINED_GLASS.getDefaultState());
        }

        return template;
    }
}
