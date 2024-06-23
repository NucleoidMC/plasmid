package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;
import java.util.function.Consumer;

public final class JankGame {
    private static ChestBoatEntity FAKE_BOAT = new ChestBoatEntity(PolymerCommonUtils.getFakeWorld(), 0, 0, 0);
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
            activity.listen(GamePlayerEvents.OFFER, offer ->
                    offer.accept(world, new Vec3d(0.0, 65.0, 0.0))
                            .thenRun(player -> player.changeGameMode(GameMode.ADVENTURE))
            );

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

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });
            var mover = new ArmorStandEntity(gameSpace.getWorlds().iterator().next(), 0.0, 65.0, 0.0);
            gameSpace.getWorlds().iterator().next().spawnEntity(mover);

            activity.listen(GamePlayerEvents.ADD, player -> {
                player.networkHandler.sendPacket(FAKE_BOAT.createSpawnPacket());
                player.networkHandler.sendPacket(CAMERA.createSpawnPacket());
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
                    return ActionResult.FAIL;
                } else if (packet instanceof VehicleMoveC2SPacket vehicleMoveC2SPacket) {
                    currentX = vehicleMoveC2SPacket.getYaw();
                    currentY = vehicleMoveC2SPacket.getZ();
                    updateSidebar.accept(player);
                    return ActionResult.FAIL;
                } else if (packet instanceof PlayerInputC2SPacket playerInputC2SPacket) {
                    space = playerInputC2SPacket.isJumping();
                    shift = playerInputC2SPacket.isSneaking();
                    return ActionResult.FAIL;
                }
                
                return ActionResult.PASS;
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
                player.networkHandler.sendPacket(new EntityPositionS2CPacket(CAMERA));
                player.networkHandler.sendPacket(new EntityPositionS2CPacket(FAKE_BOAT));
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(FAKE_BOAT));
                player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(0, 0, 0, 0, 0f, Set.of(), 0));
            });

            activity.listen(GamePlayerEvents.OFFER, offer ->
                    offer.accept(gameSpace.getWorlds().iterator().next(), new Vec3d(0.0, 65.0, 0.0))
                            .thenRun(joiningPlayer -> joiningPlayer.changeGameMode(GameMode.ADVENTURE))
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
