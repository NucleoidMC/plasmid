package xyz.nucleoid.plasmid.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
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
        var biome =  context.server().registryAccess().getOrThrow(Biomes.CHERRY_GROVE);

        var worldConfig = new RuntimeLevelConfig()
                .setGenerator(new FlatLevelSource(new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of())))
                //.setTimeOfDay(6000)
                .setGameRule(GameRules.KEEP_INVENTORY, true);

        return context.open((activity) -> {
            var gameSpace = activity.getGameSpace();
            var world = gameSpace.getLevels().addPersistent(Identifier.fromNamespaceAndPath("testmod", "persistent_world"), worldConfig);


            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, acceptor ->
                    acceptor.teleport(world, new Vec3(0.0, 65.0, 0.0))
                            .thenRunForEach(joiningPlayer -> {
                                joiningPlayer.setGameMode(GameType.CREATIVE);
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
