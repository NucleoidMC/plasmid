package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class TestGameWithResourcePack {
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(Plasmid.id("test_rp"));

    private static final GameTeam TEAM = new GameTeam(
            new GameTeamKey("players"),
            GameTeamConfig.builder()
                    .setNameTagVisibility(Team.Visibility.NEVER)
                    .build()
    );

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        var template = generateMapTemplate();

        var worldConfig = new RuntimeLevelConfig()
                .setGenerator(new TemplateChunkGenerator(context.server(), template))
                //.setTimeOfDay(6000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        return context.openWithLevel(worldConfig, (activity, world) -> {
            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.setGameMode(GameType.ADVENTURE);
                            })
            );

            GameWaitingLobby.addTo(activity, new WaitingLobbyConfig(1, 99));

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
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
                BlockMapper.set(player.connection, TestInitializer.BLOCK_CREATOR.getBlockMapper());
                PolymerUtils.reloadWorld(player);
            });

            var sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(Component.translatable("text.test.test"));

            activity.listen(GameActivityEvents.TICK, () -> {
                long time = gameSpace.getTime() - currentTime;
                if (time % 20 == 0) {
                    sidebar.set(b -> {
                        b.add(Component.literal("Hello Level! " + ((400 - time) / 20) + "s").setStyle(Style.EMPTY.withColor(0xFF0000)));
                        b.add(CommonComponents.EMPTY);
                        b.add(Component.translatable("text.plasmid.game.started.player", "test"));
                    });

                    GameStatisticBundle statistics = gameSpace.getStatistics().bundle("plasmid_test_game");
                    for (ServerPlayer player : gameSpace.getPlayers()) {
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

            var world = gameSpace.getLevels().iterator().next();

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });


            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(gameSpace.getLevels().iterator().next(), new Vec3(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.setGameMode(GameType.ADVENTURE);
                            })
            );
        });

        return GameResult.ok();
    }

    private static MapTemplate generateMapTemplate() {
        var template = MapTemplate.createEmpty();

        for (var pos : BlockBounds.of(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, TestInitializer.TEST_BLOCK.defaultBlockState());
        }

        return template;
    }
}
