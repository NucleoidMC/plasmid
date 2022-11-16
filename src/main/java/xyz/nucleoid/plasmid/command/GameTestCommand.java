package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import xyz.nucleoid.plasmid.command.argument.GameConfigArgument;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameTestCommand {
    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("test")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(GameConfigArgument.argument("game_config")
                        .executes(GameTestCommand::openTestGame)
                    )
                    .then(argument("game_config_nbt", NbtCompoundArgumentType.nbtCompound())
                        .executes(GameTestCommand::openAnonymousTestGame)
                    )
                )
        );
    }
    // @formatter:on

    private static int openTestGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return GameCommand.openGame(context, true);
    }

    private static int openAnonymousTestGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return GameCommand.openAnonymousGame(context, true);
    }
}
