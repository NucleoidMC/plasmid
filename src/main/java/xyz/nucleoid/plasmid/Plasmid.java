package xyz.nucleoid.plasmid;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import xyz.nucleoid.plasmid.command.CustomizeCommand;
import xyz.nucleoid.plasmid.command.GameCommand;
import xyz.nucleoid.plasmid.command.MapCommand;
import xyz.nucleoid.plasmid.command.PartyCommand;
import xyz.nucleoid.plasmid.entity.CustomEntity;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.map.template.StagingBoundRenderer;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleChunkGenerator;
import xyz.nucleoid.plasmid.item.CustomItem;
import xyz.nucleoid.plasmid.item.PlasmidCustomItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(PlasmidCustomItems.class);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(ID, "bubble"), BubbleChunkGenerator.CODEC);

        GameConfigs.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CustomizeCommand.register(dispatcher);
            MapCommand.register(dispatcher);
            GameCommand.register(dispatcher);
            PartyCommand.register(dispatcher);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                ItemStack stack = player.getStackInHand(hand);
                CustomItem custom = CustomItem.match(stack);
                if (custom != null) {
                    TypedActionResult<ItemStack> result = custom.onUse(player, world, hand);
                    if (result.getResult().isAccepted()) {
                        return result;
                    }
                }

                GameWorld gameWorld = GameWorld.forWorld(world);
                if (gameWorld != null && gameWorld.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameWorld.testRule(GameRule.INTERACT) == RuleResult.DENY) {
                        return TypedActionResult.fail(ItemStack.EMPTY);
                    }

                    UseItemListener invoker = gameWorld.invoker(UseItemListener.EVENT);
                    return invoker.onUseItem((ServerPlayerEntity) player, hand);
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient) {
                GameWorld gameWorld = GameWorld.forWorld(world);
                if (gameWorld != null && gameWorld.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameWorld.testRule(GameRule.INTERACT) == RuleResult.DENY) {
                        return ActionResult.FAIL;
                    }

                    UseBlockListener invoker = gameWorld.invoker(UseBlockListener.EVENT);
                    return invoker.onUseBlock((ServerPlayerEntity) player, hand, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                GameWorld gameWorld = GameWorld.forWorld(world);
                if (gameWorld != null && gameWorld.containsPlayer(serverPlayer)) {
                    AttackEntityListener invoker = gameWorld.invoker(AttackEntityListener.EVENT);
                    return invoker.onAttackEntity(serverPlayer, hand, entity, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                GameWorld gameWorld = GameWorld.forWorld(world);
                if (gameWorld != null && gameWorld.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameWorld.testRule(GameRule.INTERACT) == RuleResult.DENY) {
                        return ActionResult.FAIL;
                    }
                }

                CustomEntity customEntity = CustomEntity.match(entity);
                if (customEntity != null) {
                    return customEntity.interact(player, world, hand, entity, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            GameWorld game = GameWorld.forWorld(world);
            if (game != null) {
                game.tick();
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(StagingBoundRenderer::onTick);
    }
}
