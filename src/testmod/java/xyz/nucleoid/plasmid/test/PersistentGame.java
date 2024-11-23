package xyz.nucleoid.plasmid.test;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.List;
import java.util.Optional;

public final class PersistentGame {
    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        var biome =  context.server().getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.CHERRY_GROVE);

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(new FlatChunkGenerator(new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of())))
                .setTimeOfDay(6000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        return context.open((activity) -> {
            var gameSpace = activity.getGameSpace();
            var world = gameSpace.getWorlds().addPersistent(Identifier.of("testmod", "persistent_world"), worldConfig);


            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3d(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.changeGameMode(GameMode.CREATIVE);
                            })
            );


            activity.allow(GameRuleType.PVP).allow(GameRuleType.MODIFY_ARMOR);
            activity.deny(GameRuleType.FALL_DAMAGE).deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS);

            activity.listen(PlayerDeathEvent.EVENT, (player, source) -> {
                player.setPos(0.0, 65.0, 0.0);
                return EventResult.DENY;
            });
        });
    }
}
