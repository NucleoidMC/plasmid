package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.server.permissions.PermissionLevel;
import xyz.nucleoid.plasmid.impl.command.argument.GameConfigArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static xyz.nucleoid.plasmid.impl.Plasmid.id;

public final class GameTestCommand {
    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("test")
                    .requires(PermissionPredicates.require(id("command/game/test"), PermissionLevel.GAMEMASTERS))
                    .then(GameConfigArgument.argument("game_config")
                        .executes(GameTestCommand::openTestGame)
                    )
                    .then(argument("game_config_nbt", CompoundTagArgument.compoundTag())
                        .executes(GameTestCommand::openAnonymousTestGame)
                    )
                )
        );
    }
    // @formatter:on

    private static int openTestGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return GameCommand.openGame(context, true);
    }

    private static int openAnonymousTestGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return GameCommand.openAnonymousGame(context, true);
    }
}
