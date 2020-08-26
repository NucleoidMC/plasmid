package xyz.nucleoid.plasmid.chat.translation;

public class PlasmidTranslations {

    static {
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.started.player", "%s has started the game!");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.stopped.player", "%s has stopped the game!");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.stopped.error", "An unexpected error was thrown while stopping the game!");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.command.list", "Registered games:");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.channel.create", "Created channel with id '%s'");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.channel.remove", "Removed channel with id '%s'");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.channel.connect.entity", "Connected '%1$s' to '%2$s''");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.channel.connect.block", "Connected '%1$s' to block at (%2$s; %3$s; %4$s)");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.join", "%s has joined the game lobby!");
        TranslationHandler.registerDefaultTranslation("text.plasmid.game.join.error", "An unexpected exception occurred while joining game!");
    }
}
