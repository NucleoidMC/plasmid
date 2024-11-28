package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.sidebars.api.SidebarUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
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
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;
import java.util.function.Consumer;

public final class JankGame {
    private static ArmorStandEntity CAMERA = new ArmorStandEntity(PolymerCommonUtils.getFakeWorld(), 0, 80, 0);
    private volatile static float currentYaw;
    private volatile static float currentPitch;
    private volatile static float currentX;
    private volatile static float currentXOld;
    private volatile static double currentY;
    private volatile static PlayerInput input = PlayerInput.DEFAULT;

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

            GameWaitingLobby.addTo(activity, new WaitingLobbyConfig(1, 99));

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
                    var text = Text.empty();
                    text.append(Text.literal("^").formatted(input.forward() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal("v").formatted(input.backward() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal("<").formatted(input.left() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal(">").formatted(input.right() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal("-").formatted(input.jump() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal("_").formatted(input.sneak() ? Formatting.GREEN : Formatting.DARK_GRAY));
                    text.append(Text.literal("$").formatted(input.sprint() ? Formatting.GREEN : Formatting.DARK_GRAY));

                    sidebar.set(b -> {
                        b.add(Text.literal("YAW: " + currentYaw));
                        b.add(Text.literal("PITCH: " + currentPitch));
                        b.add(Text.literal("Mouse-X: " + currentX));
                        b.add(Text.literal("Mouse-Y: " + currentY));
                        b.add(text);
                    });

                SidebarUtils.updateTexts(player.networkHandler, sidebar);
            };

            var world = gameSpace.getWorlds().iterator().next();

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });
            var mover = new ArmorStandEntity(world, 0.0, 65.0, 0.0);
            world.spawnEntity(mover);

            PlayerLimiter.addTo(activity, new PlayerLimiterConfig(24));

            activity.listen(GameActivityEvents.STATE_UPDATE, state -> state
                    .spectators(4)
                    .state(GameSpaceState.State.STARTING)
            );

            activity.listen(GamePlayerEvents.ADD, player -> {
                player.networkHandler.sendPacket(CAMERA.createSpawnPacket(new EntityTrackerEntry(world, CAMERA, 1, false, player.networkHandler::sendPacket)));
                player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(CAMERA.getId(), CAMERA.getDataTracker().getChangedEntries()));
                player.networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(CAMERA.getId(), IntList.of(player.getId())));
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
                } else if (packet instanceof PlayerInputC2SPacket playerInputC2SPacket) {
                    input = playerInputC2SPacket.input();
                    updateSidebar.accept(player);
                    return EventResult.DENY;
                }
                
                return EventResult.PASS;
            }));

            var player = gameSpace.getPlayers().iterator().next();

            activity.listen(GameActivityEvents.TICK, () -> {
                mover.move(MovementType.PLAYER, new Vec3d(input.right() ? -1 : input.left() ? 1 : 0,
                        input.jump() ? 3 : input.sneak() ? -1 : 0,
                        input.forward() ? 1 : input.backward() ? -1 : 0).multiply(input.sprint() ? 0.4 : 0.2));
                mover.setYaw(currentYaw);


                JankGame.mouseX = MathHelper.clamp(-currentYaw / 90 * 2, -8, 8) + mover.getX();
                JankGame.mouseY = MathHelper.clamp(-currentPitch / 90 * 2, -8, 8) + mover.getZ();

                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.FLAME, true, true, JankGame.mouseX, mover.getY(), JankGame.mouseY, 0, 0, 0, 0, 0));

                CAMERA.setPos(mover.getX(), mover.getY() + 10, mover.getZ());
                player.networkHandler.sendPacket(EntityPositionSyncS2CPacket.create(CAMERA));
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
        CAMERA.setNoGravity(true);
        CAMERA.setMarker(true);
        CAMERA.setInvisible(true);
    }
}
