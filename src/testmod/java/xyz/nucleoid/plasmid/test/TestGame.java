package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapEntity;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.team.*;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig.Colors;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.api.util.WoodType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestGame {
    private static final BlockState BUTTON = Blocks.OAK_BUTTON.getDefaultState().with(ButtonBlock.FACE, BlockFace.FLOOR);
    private static final List<Method> WOOD_TYPE_BLOCK_FIELDS = Arrays.stream(WoodType.class.getMethods()).filter(x -> x.getReturnType() == Block.class).toList();
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(Identifier.of(Plasmid.ID, "test"));

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
            var gameSpace = activity.getGameSpace();

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3d(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.changeGameMode(GameMode.ADVENTURE);
                            })
            );

            GameWaitingLobby.addTo(activity, context.config().players());

            int teamCount = context.config().teamCount();

            if (teamCount > 0) {
                var random = world.getRandom();
                var teams = new ArrayList<GameTeam>();

                for (int i = 0; i < teamCount; i++) {
                    var dyeColor = Util.getRandom(DyeColor.values(), random);
                    var color = Colors.from(dyeColor);

                    var name = Text.literal("<Team " + i + ">");

                    var key = new GameTeamKey("team_" + i);

                    var config = GameTeamConfig.builder()
                        .setName(name)
                        .setColors(color)
                        .build();

                    teams.add(new GameTeam(key, config));
                }

                TeamSelectionLobby.addTo(activity, new GameTeamList(teams));
            }

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS);

            // Waiting lobbies disable interaction, so the rule must be re-enabled for the event to be invoked
            activity.allow(GameRuleType.INTERACTION);

            activity.listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
                var state = player.getWorld().getBlockState(hitResult.getBlockPos());

                if (state == BUTTON) {
                    // These should be mutually exclusive
                    boolean spectator = gameSpace.getPlayers().spectators().contains(player);
                    boolean participant = gameSpace.getPlayers().participants().contains(player);

                    if (spectator && participant) {
                        player.sendMessage(Text.empty().append(player.getDisplayName()).append(" is both a spectator and participant... somehow..."));
                    } else if (spectator) {
                        player.sendMessage(Text.empty().append(player.getDisplayName()).append(" is a spectator"));
                    } else if (participant) {
                        player.sendMessage(Text.empty().append(player.getDisplayName()).append(" is a participant"));
                    }
                }

                return ActionResult.PASS;
            });

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(gameSpace));

        });
    }

    private static final GameAttachment<Item> TEST = GameAttachment.create(Identifier.of("plasmid", "test"));

    private static GameResult startGame(GameSpace gameSpace) {
        gameSpace.setAttachment(TEST, Items.POTATO);

        gameSpace.setActivity((activity) -> {
            long currentTime = gameSpace.getTime();
            activity.deny(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.STOP_SPECTATING_ENTITY);

            activity.deny(GameRuleType.INTERACTION).allow(GameRuleType.USE_BLOCKS);

            Item potato = gameSpace.getAttachment(TEST);

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
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });

            /*var world = gameSpace.getWorlds().iterator().next();

            activity.listen(GamePlayerEvents.OFFER, offer -> {
                var player = offer.player();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.ADVENTURE));
            });*/
        });

        return GameResult.ok();
    }

    private static MapTemplate generateMapTemplate(BlockState state) {
        var template = MapTemplate.createEmpty();

        var bounds = BlockBounds.of(-5, 64, -5, 5, 64, 5);
        var max = bounds.max();

        var edge = new BlockPos(max.getX(), max.getY() + 1, max.getZ());
        template.setBlockState(edge, BUTTON);

        var armorStandNbt = new NbtCompound();
        armorStandNbt.putString("id", EntityType.getId(EntityType.ARMOR_STAND).toString());
        armorStandNbt.putBoolean("NoGravity", true);

        var armorStandPos = Vec3d.ofBottomCenter(edge.offset(Direction.WEST));
        template.addEntity(new MapEntity(armorStandPos, armorStandNbt));

        for (var pos : bounds) {
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
