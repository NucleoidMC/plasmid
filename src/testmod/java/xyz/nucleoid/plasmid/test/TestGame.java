package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.*;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.WoodType;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class TestGame {
    private static final List<Method> WOOD_TYPE_BLOCK_FIELDS = Arrays.stream(WoodType.class.getMethods()).filter(x -> x.getReturnType() == Block.class).toList();
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(new Identifier(Plasmid.ID, "test"));

    private static final GameTeam TEAM = new GameTeam(
            new GameTeamKey("players"),
            GameTeamConfig.builder()
                    .setNameTagVisibility(AbstractTeam.VisibilityRule.NEVER)
                    .build()
    );

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        var template = TestGame.generateMapTemplate(context.game().config().state());

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(new TemplateChunkGenerator(context.server(), template))
                .setTimeOfDay(6000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            activity.listen(GamePlayerEvents.OFFER, offer -> {
                var player = offer.player();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.ADVENTURE));
            });

            GameWaitingLobby.addTo(activity, new PlayerConfig(1, 99));

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(activity.getGameSpace()));

        });
    }

    private static GameResult startGame(GameSpace gameSpace) {
        gameSpace.setAttachment("test", Items.POTATO);

        gameSpace.setActivity((activity) -> {
            long currentTime = gameSpace.getTime();
            activity.deny(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.deny(GameRuleType.INTERACTION).allow(GameRuleType.USE_BLOCKS);

            Item potato = gameSpace.getAttachment("test");

            var teamManager = TeamManager.addTo(activity);
            teamManager.addTeam(TEAM);

            TeamChat.addTo(activity, teamManager);

            activity.listen(GamePlayerEvents.ADD, player -> teamManager.addPlayerTo(player, TEAM.key()));

            var sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(Text.translatable("text.test.test"));

            activity.listen(GameActivityEvents.TICK, () -> {
                long time = gameSpace.getTime() - currentTime;
                if (time % 20 == 0) {
                    sidebar.set(b -> {
                        b.add(Text.literal("Hello World! " + (time / 20) + "s").setStyle(Style.EMPTY.withColor(0xFF0000)));
                        b.add(ScreenTexts.EMPTY);
                        b.add(Text.translatable("text.plasmid.game.started.player", "test"));
                    });

                    GameStatisticBundle statistics = gameSpace.getStatistics().bundle("plasmid_test_game");
                    for (ServerPlayerEntity player : gameSpace.getPlayers()) {
                        statistics.forPlayer(player).increment(TEST_KEY, 2.5);
                    }
                }

                if (time > 500) {
                    gameSpace.close(GameCloseReason.FINISHED);
                }
            });

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            /*activity.listen(GamePlayerEvents.OFFER, offer -> {
                var player = offer.player();
                return offer.accept(gameSpace.getWorlds().iterator().next(), new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.ADVENTURE));
            });*/
        });

        return GameResult.ok();
    }

    private static MapTemplate generateMapTemplate(BlockState state) {
        var template = MapTemplate.createEmpty();

        for (var pos : BlockBounds.of(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, state);
        }

        try {
            var mut = new BlockPos.Mutable();
            mut.setZ(16);
            int y = 66 + WoodType.values().length;
            for (var type : WoodType.values()) {
                int x = 0;
                mut.setY(y);
                for (var field : WOOD_TYPE_BLOCK_FIELDS) {
                    state = ((Block) field.invoke(type)).getDefaultState().withIfExists(LeavesBlock.PERSISTENT, true);
                    template.setBlockState(mut.setX(x), state);
                    x++;
                }
                y--;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        return template;
    }
}
