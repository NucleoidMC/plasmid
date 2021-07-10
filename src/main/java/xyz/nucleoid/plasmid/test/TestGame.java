package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class TestGame {
    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        MapTemplate template = TestGame.generateMapTemplate();

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(template.asChunkGenerator(context.getServer()))
                .setTimeOfDay(6000)
                .setGameRule(GameRules.DO_MOB_SPAWNING, false)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            activity.listen(GamePlayerEvents.OFFER, offer -> {
                ServerPlayerEntity player = offer.getPlayer();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.ADVENTURE));
            });

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            SidebarWidget sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(new TranslatableText("text.plasmid.test"));

            GameSpace gameSpace = activity.getGameSpace();

            activity.listen(GameActivityEvents.TICK, () -> {
                long time = gameSpace.getTime();
                if (time % 20 == 0) {
                    sidebar.set(content -> {
                        content.writeLine("Hello World! " + (time / 20) + "s");
                        content.writeLine("");
                        content.writeTranslated("text.plasmid.game.started.player", "test");
                    });
                }

                if (time > 100) {
                    gameSpace.close(GameCloseReason.FINISHED);
                }
            });
        });
    }

    private static MapTemplate generateMapTemplate() {
        MapTemplate template = MapTemplate.createEmpty();

        for (BlockPos pos : BlockPos.iterate(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, Blocks.BLUE_STAINED_GLASS.getDefaultState());
        }

        return template;
    }
}
