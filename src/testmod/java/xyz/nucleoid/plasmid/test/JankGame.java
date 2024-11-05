package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.JoinOffer;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;
import java.util.function.Consumer;

public final class JankGame {
    private static ChestBoatEntity FAKE_BOAT = new ChestBoatEntity(EntityType.OAK_CHEST_BOAT, PolymerCommonUtils.getFakeWorld(), () -> Items.OAK_CHEST_BOAT);
    private static ArmorStandEntity CAMERA = new ArmorStandEntity(PolymerCommonUtils.getFakeWorld(), 0, 80, 0);
    private volatile static float currentYaw;
    private volatile static float currentPitch;
    private volatile static float currentX;
    private volatile static float currentXOld;
    private volatile static double currentY;
    private volatile static boolean shift;
    private volatile static boolean space;

    private static double mouseX = 0;
    private static double mouseY = 0;

    public static GameOpenProcedure open(GameOpenContext<TestConfig> context) {
        var template = JankGame.generateMapTemplate(context.game().config().state());

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
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });

            activity.listen(GameActivityEvents.REQUEST_START, () -> startGame(activity.getGameSpace()));

        });
    }

    private static GameResult startGame(GameSpace gameSpace) {
        gameSpace.setActivity((activity) -> {
            activity.deny(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS).deny(GameRuleType.MODIFY_INVENTORY);
            CAMERA.setPos(0, 70, 0);
            CAMERA.setPitch(90);
            CAMERA.setYaw(0);
            activity.deny(GameRuleType.INTERACTION).allow(GameRuleType.USE_BLOCKS);

            var sidebar = GlobalWidgets.addTo(activity)
                    .addSidebar(Text.translatable("text.test.test"));
            sidebar.setUpdateRate(99999999);

            Consumer<ServerPlayerEntity> updateSidebar = (player) -> {
                    sidebar.set(b -> {
                        b.add(Text.literal("     "));
                        b.add(Text.literal("YAW: " + currentYaw));
                        b.add(Text.literal("PITCH: " + currentPitch));
                        b.add(Text.literal("X: " + currentX));
                        b.add(Text.literal("Y: " + currentY));
                        b.add(Text.literal("Space: " + space));
                        b.add(Text.literal("Shift: " + shift));
                    });

                //SidebarUtils.updateTexts(player.networkHandler, sidebar);
            };

            var world = gameSpace.getWorlds().iterator().next();

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });
            var mover = new ArmorStandEntity(world, 0.0, 65.0, 0.0);
            world.spawnEntity(mover);

            activity.listen(GamePlayerEvents.ADD, player -> {
                player.networkHandler.sendPacket(FAKE_BOAT.createSpawnPacket(new EntityTrackerEntry(world, FAKE_BOAT, 1, false, player.networkHandler::sendPacket)));
                player.networkHandler.sendPacket(CAMERA.createSpawnPacket(new EntityTrackerEntry(world, CAMERA, 1, false, player.networkHandler::sendPacket)));
                player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(FAKE_BOAT.getId(), FAKE_BOAT.getDataTracker().getChangedEntries()));
                player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(CAMERA.getId(), CAMERA.getDataTracker().getChangedEntries()));
                player.networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(FAKE_BOAT.getId(), IntList.of(player.getId())));
                player.networkHandler.sendPacket(new SetCameraEntityS2CPacket(CAMERA));
            });
            
            activity.listen(PlayerC2SPacketEvent.EVENT, ((player, packet) -> {
                if (packet instanceof PlayerMoveC2SPacket rot) {
                    if (rot.changesLook()) {
                        currentYaw = rot.getYaw(currentYaw);
                        currentPitch = rot.getPitch(currentPitch);
                    }

                    updateSidebar.accept(player);
                    return EventResult.DENY;
                } else if (packet instanceof VehicleMoveC2SPacket vehicleMoveC2SPacket) {
                    currentX = vehicleMoveC2SPacket.getYaw();
                    currentY = vehicleMoveC2SPacket.getZ();
                    updateSidebar.accept(player);
                    return EventResult.DENY;
                } else if (packet instanceof PlayerInputC2SPacket playerInputC2SPacket) {
                    space = playerInputC2SPacket.input().jump();
                    shift = playerInputC2SPacket.input().sneak();
                    return EventResult.DENY;
                }
                
                return EventResult.PASS;
            }));

            var player = gameSpace.getPlayers().iterator().next();

            activity.listen(GameActivityEvents.TICK, () -> {
                boolean isMoving = Math.abs(currentX) > 9 || (Math.abs(currentX) - Math.abs(currentXOld) > 0 && Math.abs(currentX) > 0.1);
                currentXOld = currentX;

                mover.move(MovementType.PLAYER, new Vec3d(isMoving ? -Math.signum(currentX) : 0, space ? 1 : shift ? -1 : 0, currentY > 0.02 ? 1 : currentY < -0.003 ? -1 : 0).multiply(0.1));
                mover.setYaw(currentYaw);


                JankGame.mouseX += Math.abs(currentYaw) > 0.1 ? -currentYaw * 0.1 : 0;
                JankGame.mouseY += Math.abs(currentPitch) > 0.1 ? -currentPitch * 0.1 : 0;

                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.FLAME, true, JankGame.mouseX, 65, JankGame.mouseY, 0, 0, 0, 0, 0));

                player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(FAKE_BOAT.getId(), FAKE_BOAT.getDataTracker().getChangedEntries()));
                player.networkHandler.sendPacket(new VehicleMoveS2CPacket(FAKE_BOAT));
                CAMERA.setPos(mover.getX(), 70, mover.getZ());
                player.networkHandler.sendPacket(EntityPositionSyncS2CPacket.create(CAMERA));
                player.networkHandler.sendPacket(EntityPositionSyncS2CPacket.create(FAKE_BOAT));
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(FAKE_BOAT));
                player.networkHandler.sendPacket(PlayerPositionLookS2CPacket.of(0, new PlayerPosition(Vec3d.ZERO, Vec3d.ZERO, 0, 0f), Set.of()));
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

    private static MapTemplate generateMapTemplate(BlockState state) {
        var template = MapTemplate.createEmpty();

        for (var pos : BlockBounds.of(-40, 64, -40, 40, 64, 40)) {
            template.setBlockState(pos, state);
        }

        return template;
    }

    static {
        FAKE_BOAT.setNoGravity(true);
        FAKE_BOAT.setInvisible(true);
        CAMERA.setNoGravity(true);
        CAMERA.setMarker(true);
        CAMERA.setInvisible(true);
    }
}
