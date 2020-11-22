package xyz.nucleoid.plasmid.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.UserCache;
import xyz.nucleoid.plasmid.storage.FriendList;
import xyz.nucleoid.plasmid.storage.FriendListManager;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


/*
    some breaking problems are if a user cache name expires, the whole thing breaks
    at the moment i have no idea how to fix this besides saving player names ourselves
 */

public class FriendCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("friend")
                .then(literal("accept").then(argument("player", GameProfileArgumentType.gameProfile()).executes(FriendCommand::acceptFriend)))
                .then(literal("add").then(argument("playerName", GameProfileArgumentType.gameProfile()).executes(FriendCommand::registerFriendAdd)))
                .then(literal("list").executes(FriendCommand::ListFriends))
                .then(literal("requests").executes(FriendCommand::ListRequests)));
    }

    private static int ListRequests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();
        ServerPlayerEntity player = context.getSource().getPlayer();
        UserCache userCache = server.getUserCache();

        if (FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).returnRequestList().size() <= 0) {
            player.sendMessage(new LiteralText("No incoming friend requests"), false);
        }

        int i = 0;
        player.sendMessage(new LiteralText("### incoming friend requests ###"), false);
        for (UUID ids : FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).returnRequestList()) {
            i++;
            player.sendMessage(new LiteralText("[" + i + "] Request from " + userCache.getByUuid(ids).getName()), false);
        }
        player.sendMessage(new LiteralText("### incoming friend requests ###"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int ListFriends(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();
        ServerPlayerEntity player = context.getSource().getPlayer();
        UserCache uCache = server.getUserCache();

        if (FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).returnFlistIds().size() <= 0) {
            player.sendMessage(new LiteralText("You have no friends..."), false);
            return Command.SINGLE_SUCCESS;
        }

        int i = 0;
        FriendList friendList = FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid());

        for (UUID ids : friendList.returnFlistIds()) {
            i++;
            player.sendMessage(new LiteralText("[" + i + "] " + uCache.getByUuid(ids).getName()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int acceptFriend(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // this will break if more then one GameProfile is specified
        for (GameProfile Profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
            // TODO: prettify this code
            FriendList target = FriendListManager.returnFriendlist(Profile.getId());
            FriendList sender = FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid());

            if (FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).requestListContains(Profile.getId())) {
                sender.addFriend(Profile.getId());
                target.addFriend(context.getSource().getPlayer().getUuid());
                sender.removeRequest(Profile.getId()); // not needed but you never know
                target.removeRequest(context.getSource().getPlayer().getUuid());
                context.getSource().getPlayer().sendMessage(new LiteralText("You accepted " + Profile.getName() + "friend request."), false);
                context.getSource().getMinecraftServer().getPlayerManager().getPlayer(Profile.getId()).sendMessage(new LiteralText(context.getSource().getName() + " accepted your friend request"), false);
            } else {
                context.getSource().getPlayer().sendMessage(new LiteralText("There are no incoming requests from that player"), false);
            }
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int registerFriendAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity commandSender = context.getSource().getPlayer();
        MinecraftServer server = context.getSource().getMinecraftServer();
        // this will not break if more then one GameProfile is specified but it wil send it to ALL specified, so pls no do ty
        for (GameProfile Profile : GameProfileArgumentType.getProfileArgument(context, "playerName")) {
            if (Profile.equals(context.getSource().getPlayer().getGameProfile())) {
                context.getSource().getPlayer().sendMessage(new LiteralText("You can't friend yourself silly!"), false);
                return Command.SINGLE_SUCCESS;
            }
            if (FriendListManager.returnFriendlist(commandSender.getUuid()).hasFriend(Profile.getId())) {
                commandSender.sendMessage(new LiteralText("You are already friends with that player"), false);
                return Command.SINGLE_SUCCESS;
            }
            if (FriendListManager.returnFriendlist(Profile.getId()).addRequests(commandSender.getUuid())) {
                commandSender.sendMessage(new LiteralText("You sent a request to " + Profile.getName()), false);
                server.getPlayerManager().getPlayer(Profile.getId()).sendMessage(new LiteralText("You received a friend request from " + server.getUserCache().getByUuid(commandSender.getUuid()).getName()), false);
            } else {
                commandSender.sendMessage(new LiteralText("You already sent that player a request."), false);
                return 0;
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
