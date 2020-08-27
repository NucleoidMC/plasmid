package xyz.nucleoid.plasmid.chat.translation;

import fr.catcore.server.translations.ServerTranslations;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class TranslationHandler {

    private static final List<String> SERVER_SIDE_KEYS = new ArrayList<>();

    private static boolean languageLoaded = false;

    public static void registerDefaultTranslation(String key, String value) {
        SERVER_SIDE_KEYS.add(key);
        if (!FabricLoader.getInstance().isModLoaded("server_translations")) {
            if (!languageLoaded) {
                Language.getInstance();
                languageLoaded = true;
            }
            ((ExtendedLanguage) Language.getInstance()).registerTranslations(key, value);
        }
    }

    public static Text getCorrectText(TranslatableText translatableText, ServerPlayerEntity serverPlayerEntity) {
        if (FabricLoader.getInstance().isModLoaded("server_translations")
                && SERVER_SIDE_KEYS.contains(translatableText.getKey())) {
            return ServerTranslations.getMessageForPlayer(translatableText, serverPlayerEntity);
        }
        if (SERVER_SIDE_KEYS.contains(translatableText.getKey())) {
            Style style = translatableText.getStyle();
            String string = translatableText.getString();
            List<Text> siblings = new ArrayList<>();
            for (Text text : translatableText.getSiblings()) {
                string = string.replace(text.getString(), "");
                if (text instanceof TranslatableText) siblings.add(new LiteralText(text.getString()).setStyle(text.getStyle()));
                else if (text instanceof BaseText) {
                    for (Text text1 : translatableText.getSiblings()) {
                        string = string.replace(text1.getString(), "");
                        if (text1 instanceof TranslatableText) siblings.add(new LiteralText(text1.getString()).setStyle(text1.getStyle()));
                        else {
                            siblings.add(text1);
                        }

                    }
                } else {
                    siblings.add(text);
                }
            }
            MutableText literalText = new LiteralText(string).setStyle(style);
            for (Text sibling : siblings) {
                literalText = literalText.append(sibling);
            }
            return literalText;
        }
        return translatableText;
    }

    public static Text getCorrectText(ServerPlayerEntity playerEntity, String key) {
        return getCorrectText(new TranslatableText(key), playerEntity);
    }

    public static Text getCorrectText(ServerPlayerEntity playerEntity, String key, Object... args) {
        return getCorrectText(new TranslatableText(key, args), playerEntity);
    }
}
