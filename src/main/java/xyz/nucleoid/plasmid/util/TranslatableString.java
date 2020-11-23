package xyz.nucleoid.plasmid.util;

import fr.catcore.server.translations.api.LocalizableText;
import fr.catcore.server.translations.api.LocalizationTarget;
import net.minecraft.text.TranslatableText;

public class TranslatableString {
    private final String key;
    private Object[] args = new Object[0];

    public TranslatableString(String key) {
        this.key = key;
    }

    public TranslatableString(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public String localizeFor(LocalizationTarget target) {
        return LocalizableText.asLocalizedFor(new TranslatableText(this.key, this.args), target).getString();
    }

    public String getDefaultTranslation() {
        return new TranslatableText(this.key, this.args).getString();
    }
}
