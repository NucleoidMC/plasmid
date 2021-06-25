package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.mutable.MutableInt;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class TestGame {
    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        MapTemplate template = TestGame.buildTemplate();

        // TODO: overload that takes world config as a default?

        return context.open(activity -> {
            GameSpace gameSpace = activity.getGameSpace();

            // TODO: how do we make sure you can't do weird things by *not* teleporting players into a world

            // TODO: we want default game rules to be set!
            ServerWorld world = gameSpace.addWorld(
                    new RuntimeWorldConfig()
                            .setGenerator(template.asChunkGenerator(context.getServer()))
                            .setTimeOfDay(6000)
                            .setGameRule(GameRules.DO_MOB_SPAWNING, false)
                            .setGameRule(GameRules.DO_WEATHER_CYCLE, false)
            );

            activity.listen(GamePlayerEvents.OFFER, offer -> {
                ServerPlayerEntity player = offer.getPlayer();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.setGameMode(GameMode.ADVENTURE));
            });

            activity.allow(GameRule.PVP).allow(GameRule.MODIFY_ARMOR);
            activity.deny(GameRule.FALL_DAMAGE).deny(GameRule.HUNGER);
            activity.deny(GameRule.THROW_ITEMS).deny(GameRule.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            SidebarWidget sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(new TranslatableText("text.plasmid.test"));

            MutableInt timer = new MutableInt();

            activity.listen(GameActivityEvents.TICK, () -> {
                int time = timer.incrementAndGet();
                if (time % 20 == 0) {
                    sidebar.set(content -> {
                        content.writeLine("Hello World! " + (time / 20) + "s");
                        content.writeLine("");
                        content.writeTranslated("text.plasmid.game.started.player", "test");
                    });
                }
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
