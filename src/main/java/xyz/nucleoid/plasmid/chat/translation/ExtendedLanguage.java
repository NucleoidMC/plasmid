package xyz.nucleoid.plasmid.chat.translation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExtendedLanguage extends Language {

    private final Map<String, String> VANILLA_MAP;
    private final Map<String, String> MODDED_MAP;

    public ExtendedLanguage(Map<String, String> vanillaMap) {
        this.VANILLA_MAP = vanillaMap;
        this.MODDED_MAP = new HashMap<>();
    }

    @Override
    public String get(String key) {
        String value = key;
        if (this.VANILLA_MAP.containsKey(key)) value = this.VANILLA_MAP.get(key);
        if (this.MODDED_MAP.containsKey(key)) value = this.MODDED_MAP.get(key);
        return value;
    }

    @Override
    public boolean hasTranslation(String key) {
        return this.VANILLA_MAP.containsKey(key) || this.MODDED_MAP.containsKey(key);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean isRightToLeft() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public OrderedText reorder(StringVisitable text) {
        return (visitor) -> {
            return text.visit((style, string) -> {
                return TextVisitFactory.visitFormatted(string, style, visitor) ? Optional.empty() : StringVisitable.TERMINATE_VISIT;
            }, Style.EMPTY).isPresent();
        };
    }

    public void registerTranslations(String key, String value) {
        this.MODDED_MAP.put(key, value);
    }
}
