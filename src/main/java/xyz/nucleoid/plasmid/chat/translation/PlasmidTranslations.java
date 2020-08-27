package xyz.nucleoid.plasmid.chat.translation;

public class PlasmidTranslations {

    private static void register(String key, String value) {
        TranslationHandler.registerDefaultTranslation(key, value);
    }

    static {
        register("text.plasmid.game.started.player", "%s has started the game!");
        register("text.plasmid.game.stopped.player", "%s has stopped the game!");
        register("text.plasmid.game.stopped.error", "An unexpected error was thrown while stopping the game!");
        register("text.plasmid.game.command.list", "Registered games:");
        register("text.plasmid.game.channel.create", "Created channel with id '%s'");
        register("text.plasmid.game.channel.remove", "Removed channel with id '%s'");
        register("text.plasmid.game.channel.connect.entity", "Connected '%1$s' to '%2$s''");
        register("text.plasmid.game.channel.connect.block", "Connected '%1$s' to block at (%2$s; %3$s; %4$s)");
        register("text.plasmid.game.join", "%s has joined the game lobby!");
        register("text.plasmid.game.join.error", "An unexpected exception occurred while joining game!");
        register("text.plasmid.game.open.error", "The game threw an unexpected error while starting!");
        register("text.plasmid.game.open.opened", "%1$s has opened %2$s! ");
        register("text.plasmid.game.open.join", "Click here to join");
        register("text.plasmid.game.waiting_lobby.bar.waiting", "Waiting for players...");
        register("text.plasmid.game.waiting_lobby.bar.countdown", "Starting in %s seconds!");
        register("text.plasmid.game.waiting_lobby.bar.cancel", "Game start cancelled! ");
        register("text.plasmid.game.start_result.already_started", "This game has already started!");
        register("text.plasmid.game.start_result.not_enough_players", "Game does not have enough players yet!");
    }
}
