package xyz.nucleoid.plasmid.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapEntity;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.*;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.api.map.MapLoadContexts;
import xyz.nucleoid.plasmid.api.map.template.processor.TeamColorMapTemplateProcessor;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;
import xyz.nucleoid.plasmid.api.util.WoodTypeContent;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class TestGame {
    private static final BlockState BUTTON = Blocks.OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.FLOOR);
    private static final List<Method> WOOD_TYPE_BLOCK_FIELDS = Arrays.stream(net.minecraft.world.level.block.state.properties.WoodType.class.getMethods()).filter(x -> x.getReturnType() == Block.class).toList();
    private static final List<Method> COLORED_BLOCKS_METHODS = Arrays.stream(ColoredBlocks.class.getMethods()).filter(x -> x.getReturnType() == Block.class).toList();
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(Plasmid.id("test"));

    private static final GameTeam TEAM = new GameTeam(
            new GameTeamKey("players"),
            GameTeamConfig.builder()
                    .setNameTagVisibility(Team.Visibility.NEVER)
                    .build()
    );

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        return context.open(activity -> {
            var gameSpace = activity.getGameSpace();

            GameTeamList teamList = context.config().teams().map(teamListProvider -> {
                var teams = teamListProvider.get(context.server().overworld().getRandom());
                TeamSelectionLobby.addTo(activity, teams);
                return teams;
            }).orElse(null);

            var template = TestGame.generateMapTemplate(context.game().config().state(), teamList);

            if (teamList != null) {
                new TeamColorMapTemplateProcessor(List.of(DyeColor.values()))
                        .processTemplate(template, new ContextMap.Builder().withOptionalParameter(MapLoadContexts.TEAM_LIST, teamList));
            }

            var worldConfig = new RuntimeLevelConfig()
                    .setGenerator(new TemplateChunkGenerator(context.server(), template))
                    // Todo: Fantasy
                    //.setTimeOfDay(6000)
                    .setGameRule(GameRules.KEEP_INVENTORY, true);

            var world = gameSpace.getLevels().add(worldConfig);

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.setGameMode(GameType.ADVENTURE);
                            })
            );

            GameWaitingLobby.addTo(activity, context.config().players());

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS);

            // Waiting lobbies disable interaction, so the rule must be re-enabled for the event to be invoked
            activity.allow(GameRuleType.INTERACTION);

            activity.listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
                var state = player.level().getBlockState(hitResult.getBlockPos());

                if (state == BUTTON) {
                    // These should be mutually exclusive
                    boolean spectator = gameSpace.getPlayers().spectators().contains(player);
                    boolean participant = gameSpace.getPlayers().participants().contains(player);

                    if (spectator && participant) {
                        player.sendSystemMessage(Component.empty().append(player.getDisplayName()).append(" is both a spectator and participant... somehow..."));
                    } else if (spectator) {
                        player.sendSystemMessage(Component.empty().append(player.getDisplayName()).append(" is a spectator"));
                    } else if (participant) {
                        player.sendSystemMessage(Component.empty().append(player.getDisplayName()).append(" is a participant"));
                    }
                }

                return InteractionResult.PASS;
            });

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(gameSpace));
        });
    }

    private static final GameAttachment<Item> TEST = GameAttachment.create(Identifier.fromNamespaceAndPath("plasmid", "test"));

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
                    .addSidebar(Component.translatable("Component.test.test"));

            activity.listen(GameActivityEvents.TICK, () -> {
                long time = gameSpace.getTime() - currentTime;
                if (time % 20 == 0) {
                    sidebar.set(b -> {
                        b.add(Component.literal("Hello Level! " + (time / 20) + "s").setStyle(Style.EMPTY.withColor(0xFF0000)));
                        b.add(CommonComponents.EMPTY);
                        b.add(Component.translatable("Component.plasmid.game.started.player", "test"));
                    });

                    GameStatisticBundle statistics = gameSpace.getStatistics().bundle("plasmid_test_game");
                    for (ServerPlayer player : gameSpace.getPlayers()) {
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

            /*var world = gameSpace.getLevels().iterator().next();

            activity.listen(GamePlayerEvents.OFFER, offer -> {
                var player = offer.player();
                return offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                        .and(() -> player.changeGameMode(GameMode.ADVENTURE));
            });*/
        });

        return GameResult.ok();
    }

    private static MapTemplate generateMapTemplate(BlockState state, GameTeamList teamList) {
        var template = MapTemplate.createEmpty();

        var bounds = BlockBounds.of(-25, 64, -5, 5, 64, 5);
        var max = bounds.max();

        var edge = new BlockPos(max.getX(), max.getY() + 1, max.getZ());
        template.setBlockState(edge, BUTTON);

        var armorStandNbt = new CompoundTag();
        armorStandNbt.putString("id", EntityType.getKey(EntityTypes.ARMOR_STAND).toString());
        armorStandNbt.putBoolean("NoGravity", true);

        var armorStandPos = Vec3.atBottomCenterOf(edge.relative(Direction.WEST));
        armorStandNbt.store("Pos", Vec3.CODEC, armorStandPos);
        template.addEntity(new MapEntity(armorStandPos, armorStandNbt));

        for (var pos : bounds) {
            template.setBlockState(pos, state);
        }

        try {
            var mut = new BlockPos.MutableBlockPos();
            mut.setZ(16);
            int y = 66 + WoodTypeContent.values().length;
            for (var type : WoodTypeContent.values()) {
                int x = 0;
                mut.setY(y);
                for (var field : WOOD_TYPE_BLOCK_FIELDS) {
                    state = ((Block) field.invoke(type)).defaultBlockState().trySetValue(LeavesBlock.PERSISTENT, true);
                    template.setBlockState(mut.setX(x), state);
                    x--;
                }
                y--;
            }

            if (teamList == null) {
                return template;
            }
            y = 66 + DyeColor.values().length;
            int i = 0;
            for (var dyeColor : DyeColor.values()) {
                int x = -2 - WOOD_TYPE_BLOCK_FIELDS.size();
                mut.setY(y--);

                if (teamList.list().size() > i) {
                    var displayNbt = new CompoundTag();
                    displayNbt.putString("id", EntityType.getKey(EntityTypes.TEXT_DISPLAY).toString());
                    displayNbt.store("text", ComponentSerialization.CODEC, teamList.list().get(i++).config().name());
                    displayNbt.store("billboard", Display.BillboardConstraints.CODEC, Display.BillboardConstraints.VERTICAL);
                    var displayPos = Vec3.atBottomCenterOf(mut.setX(x--));
                    displayNbt.store("Pos", Vec3.CODEC, displayPos);
                    template.addEntity(new MapEntity(displayPos, displayNbt));
                }

                for (var field : COLORED_BLOCKS_METHODS) {
                    state = ((Block) field.invoke(null, dyeColor)).defaultBlockState();
                    template.setBlockState(mut.setX(x--), state);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return template;
    }
}
