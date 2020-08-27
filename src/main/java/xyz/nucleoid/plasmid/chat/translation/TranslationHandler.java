package xyz.nucleoid.plasmid.chat.translation;

import fr.catcore.server.translations.ServerTranslations;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
            return new LiteralText(translatableText.getString()).setStyle(translatableText.getStyle());
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
