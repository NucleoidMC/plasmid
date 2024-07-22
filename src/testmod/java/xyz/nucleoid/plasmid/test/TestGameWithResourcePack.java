package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
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
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.JoinOffer;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class TestGameWithResourcePack {
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(new Identifier(Plasmid.ID, "test_rp"));

    private static final GameTeam TEAM = new GameTeam(
            new GameTeamKey("players"),
            GameTeamConfig.builder()
                    .setNameTagVisibility(AbstractTeam.VisibilityRule.NEVER)
                    .build()
    );

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        var template = generateMapTemplate();

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(new TemplateChunkGenerator(context.server(), template))
                .setTimeOfDay(6000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3d(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.changeGameMode(GameMode.ADVENTURE);
                            })
            );

            GameWaitingLobby.addTo(activity, new PlayerConfig(1, 99));

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(activity.getGameSpace(), 0));

        });
    }

    private static GameResult startGame(GameSpace gameSpace, int iter) {
        gameSpace.setActivity((activity) -> {
            long currentTime = gameSpace.getTime();
            activity.deny(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.deny(GameRuleType.INTERACTION).allow(GameRuleType.USE_BLOCKS);

            var teamManager = TeamManager.addTo(activity);
            teamManager.addTeam(TEAM);

            if (iter != 3) {
                TestInitializer.resourcePack.ifPresent(pack -> pack.addTo(activity));
            }

            activity.listen(GamePlayerEvents.ADD, player -> {
                teamManager.addPlayerTo(player, TEAM.key());
                BlockMapper.set(player.networkHandler, TestInitializer.BLOCK_CREATOR.getBlockMapper());
                PolymerUtils.reloadWorld(player);
            });

            var sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(Text.translatable("text.test.test"));

            activity.listen(GameActivityEvents.TICK, () -> {
                long time = gameSpace.getTime() - currentTime;
                if (time % 20 == 0) {
                    sidebar.set(b -> {
                        b.add(Text.literal("Hello World! " + ((400 - time) / 20) + "s").setStyle(Style.EMPTY.withColor(0xFF0000)));
                        b.add(ScreenTexts.EMPTY);
                        b.add(Text.translatable("text.plasmid.game.started.player", "test"));
                    });

                    GameStatisticBundle statistics = gameSpace.getStatistics().bundle("plasmid_test_game");
                    for (ServerPlayerEntity player : gameSpace.getPlayers()) {
                        statistics.forPlayer(player).increment(TEST_KEY, 2.5);
                    }
                }

                if (time > 400) {
                    if (iter == 3) {
                        gameSpace.close(GameCloseReason.FINISHED);
                    } else {
                        startGame(gameSpace, iter + 1);
                    }
                    //
                }
            });

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(gameSpace.getWorlds().iterator().next(), new Vec3d(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.changeGameMode(GameMode.ADVENTURE);
                            })
            );
        });

        return GameResult.ok();
    }

    private static MapTemplate generateMapTemplate() {
        var template = MapTemplate.createEmpty();

        for (var pos : BlockBounds.of(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, TestInitializer.TEST_BLOCK.getDefaultState());
        }

        return template;
    }
}
