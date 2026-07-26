package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.sidebars.api.SidebarUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.PlayerLimiter;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class JankGame {
    private static ArmorStand CAMERA = new ArmorStand(PolymerCommonUtils.getFakeWorld(), 0, 80, 0);
    private volatile static float currentYaw;
    private volatile static float currentPitch;
    private volatile static float currentX;
    private volatile static float currentXOld;
    private volatile static double currentY;
    private volatile static Input input = Input.EMPTY;

    private static double mouseX = 0;
    private static double mouseY = 0;

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        var template = JankGame.generateMapTemplate(context.game().config().state());

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

            CAMERA.setId(world.getNextEntityId());

            GameWaitingLobby.addTo(activity, new WaitingLobbyConfig(1, 99));

            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });

            activity.listen(GamePlayerEvents.JOIN_MESSAGE, (player, text, text2) -> null);
            activity.listen(GamePlayerEvents.LEAVE_MESSAGE, (player, text, text2) -> null);

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(activity.getGameSpace()));

        });
    }

    private static GameResult startGame(GameSpace gameSpace) {
        gameSpace.setActivity((activity) -> {
            activity.deny(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);
            CAMERA.setPos(0, 70, 0);
            CAMERA.setXRot(90);
            CAMERA.setYRot(0);
            activity.deny(GameRuleType.INTERACTION).allow(GameRuleType.USE_BLOCKS);

            var sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(Component.translatable("text.test.test"));
            sidebar.setUpdateRate(99999999);

            Consumer<ServerPlayer> updateSidebar = (player) -> {
                    var text = Component.empty();
                    text.append(Component.literal("^").withStyle(input.forward() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal("v").withStyle(input.backward() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal("<").withStyle(input.left() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal(">").withStyle(input.right() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal("-").withStyle(input.jump() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal("_").withStyle(input.shift() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                    text.append(Component.literal("$").withStyle(input.sprint() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));

                    sidebar.set(b -> {
                        b.add(Component.literal("YAW: " + currentYaw));
                        b.add(Component.literal("PITCH: " + currentPitch));
                        b.add(Component.literal("Mouse-X: " + currentX));
                        b.add(Component.literal("Mouse-Y: " + currentY));
                        b.add(text);
                    });

                SidebarUtils.updateTexts(player.connection, sidebar);
            };

            var world = gameSpace.getLevels().iterator().next();

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });
            var mover = new ArmorStand(world, 0.0, 65.0, 0.0);
            world.addFreshEntity(mover);

            PlayerLimiter.addTo(activity, new PlayerLimiterConfig(24));

            activity.listen(GameActivityEvents.STATE_UPDATE, state -> state
                    .spectators(4)
                    .state(GameSpaceState.State.STARTING)
            );

            activity.listen(GamePlayerEvents.ADD, player -> {
                Consumer<Packet<?>> watchingSender = player.connection::send;

                player.connection.send(CAMERA.getAddEntityPacket(new ServerEntity(world, CAMERA, 1, false, new ServerEntity.Synchronizer() {
                    @Override
                    public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
                        watchingSender.accept(packet);
                    }

                    @Override
                    public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
                        watchingSender.accept(packet);
                    }

                    @Override
                    public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
                        watchingSender.accept(packet);
                    }
                })));
                player.connection.send(new ClientboundSetEntityDataPacket(CAMERA.getId(), CAMERA.getEntityData().getNonDefaultValues()));
                player.connection.send(VirtualEntityUtils.createClientboundSetPassengersPacket(CAMERA.getId(), IntList.of(player.getId())));
                player.connection.send(new ClientboundSetCameraPacket(CAMERA));
            });
            
            activity.listen(PlayerC2SPacketEvent.EVENT, ((player, packet) -> {
                if (packet instanceof ServerboundMovePlayerPacket rot) {
                    if (rot.hasRotation()) {
                        currentYaw = rot.getYRot(currentYaw);
                        currentPitch = rot.getXRot(currentPitch);
                    }

                    updateSidebar.accept(player);
                    return EventResult.DENY;
                } else if (packet instanceof ServerboundPlayerInputPacket playerInputC2SPacket) {
                    input = playerInputC2SPacket.input();
                    updateSidebar.accept(player);
                    return EventResult.DENY;
                }
                
                return EventResult.PASS;
            }));

            var player = gameSpace.getPlayers().iterator().next();

            activity.listen(GameActivityEvents.TICK, () -> {
                mover.move(MoverType.PLAYER, new Vec3(input.right() ? -1 : input.left() ? 1 : 0,
                        input.jump() ? 3 : input.shift() ? -1 : 0,
                        input.forward() ? 1 : input.backward() ? -1 : 0).scale(input.sprint() ? 0.4 : 0.2));
                mover.setYRot(currentYaw);


                JankGame.mouseX = Mth.clamp(-currentYaw / 90 * 2, -8, 8) + mover.getX();
                JankGame.mouseY = Mth.clamp(-currentPitch / 90 * 2, -8, 8) + mover.getZ();

                player.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.FLAME, true, true, JankGame.mouseX, mover.getY(), JankGame.mouseY, 0, 0, 0, 0, 0));

                CAMERA.setPos(mover.getX(), mover.getY() + 10, mover.getZ());
                player.connection.send(ClientboundEntityPositionSyncPacket.of(CAMERA));
                player.connection.send(ClientboundPlayerPositionPacket.of(0, new PositionMoveRotation(Vec3.ZERO, Vec3.ZERO, 0, 0f), Set.of()));
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

    private static MapTemplate generateMapTemplate(BlockState state) {
        var template = MapTemplate.createEmpty();

        for (var pos : BlockBounds.of(-40, 64, -40, 40, 64, 40)) {
            template.setBlockState(pos, state);
        }

        return template;
    }

    private static BiConsumer<Packet<?>, List<UUID>> getFilteredPacketSender(UUID uuid, Consumer<Packet<?>> sender) {
        return (packet, except) -> {
            if (!except.contains(uuid)) {
                sender.accept(packet);
            }
        };
    }

    static {
        CAMERA.setNoGravity(true);
        CAMERA.setMarker(true);
        CAMERA.setInvisible(true);
    }
}
