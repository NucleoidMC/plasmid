package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldSpawner;

import java.util.concurrent.CompletableFuture;

public final class TestGame {
    public static CompletableFuture<GameWorld> open(GameOpenContext<Unit> context) {
        return CompletableFuture.supplyAsync(TestGame::buildTemplate, Util.getMainWorkerExecutor())
                .thenCompose(template -> {
                    ChunkGenerator generator = new TemplateChunkGenerator(context.getServer(), template, BlockPos.ORIGIN);

                    BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                            .setGenerator(generator)
                            .setSpawner(BubbleWorldSpawner.atSurface(BlockPos.ORIGIN))
                            .setDefaultGameMode(GameMode.ADVENTURE);

                    return context.openWorld(worldConfig);
                })
                .thenApply(gameWorld -> {
                    gameWorld.openGame(game -> {
                        game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
                        game.setRule(GameRule.HUNGER, RuleResult.DENY);
                        game.setRule(GameRule.PVP, RuleResult.DENY);

                        game.on(PlayerDeathListener.EVENT, (player, source) -> {
                            player.teleport(0.0, 65.0, 0.0);
                            return ActionResult.FAIL;
                        });
                    });

                    return gameWorld;
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
