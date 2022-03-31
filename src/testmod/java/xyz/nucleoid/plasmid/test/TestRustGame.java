package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.common.rust.RustGame;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.concurrent.CompletableFuture;

public final class TestRustGame {
    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        var template = generateMapTemplate();

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(new TemplateChunkGenerator(context.server(), template))
                .setTimeOfDay(12000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        final CompletableFuture<RustGame> gameFuture = RustGame.connect();

        return context.openWithWorld(worldConfig, (activity, world) -> {
            final RustGame game = gameFuture.join();

            game.start(activity, world);

            activity.listen(GamePlayerEvents.OFFER, offer -> {
                var player = offer.player();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.SURVIVAL));
            });

            activity.deny(GameRuleType.PVP).deny(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                player.changeGameMode(GameMode.SPECTATOR);
                return ActionResult.FAIL;
            });
        });
    }

    private static MapTemplate generateMapTemplate() {
        var template = MapTemplate.createEmpty();
        template.setBlockState(new BlockPos(0, 64, 0), Blocks.BARRIER.getDefaultState());
        return template;
    }
}
