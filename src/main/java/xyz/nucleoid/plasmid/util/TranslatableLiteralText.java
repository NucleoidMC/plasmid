package xyz.nucleoid.plasmid.util;

import fr.catcore.server.translations.ServerTranslations;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

public final class TranslatableLiteralText extends LiteralText {
    private TranslatableText translatableText;

    public TranslatableLiteralText(String defaultString, String key) {
        super(defaultString);
        this.translatableText = new TranslatableText(key);
    }

    public TranslatableLiteralText(String defaultString, String key, Object... args) {
        super(defaultString);
        this.translatableText = new TranslatableText(key, args);
    }

    public TranslatableLiteralText(Text defaultString, String key) {
        super(defaultString.getString());
        this.translatableText = new TranslatableText(key);
    }

    public TranslatableLiteralText(Text defaultString, String key, Object... args) {
        super(defaultString.getString());
        this.translatableText = new TranslatableText(key, args);
    }

    public TranslatableLiteralText(String defaultString, TranslatableText translatableText) {
        super(defaultString);
        this.translatableText = translatableText;
    }

    public TranslatableLiteralText(Text defaultString, TranslatableText translatableText) {
        super(defaultString.getString());
        this.translatableText = translatableText;
    }

    @Override
    public MutableText setStyle(Style style) {
        this.translatableText.setStyle(style);
        super.setStyle(style);
        return this;
    }

    public LiteralText getText(ServerPlayerEntity serverPlayerEntity) {
        if (FabricLoader.getInstance().isModLoaded("server_translations")) {
            return ServerTranslations.getMessageForPlayer(this.translatableText, serverPlayerEntity);
        }
        return this;
    }

    @Override
    public MutableText append(Text text) {
        this.translatableText.append(text);
        super.append(text);
        return this;
    }

    @Override
    public MutableText append(String text) {
        this.translatableText.append(text);
        super.append(text);
        return this;
    }

    @Override
    public TranslatableLiteralText copy() {
        return new TranslatableLiteralText(super.copy(), this.translatableText);
    }
}
