package xyz.nucleoid.plasmid.util;

import fr.catcore.server.translations.ServerTranslations;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;

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

    @Override
    public MutableText setStyle(Style style) {
        this.translatableText.setStyle(style);
        return super.setStyle(style);
    }

    public LiteralText getText(ServerPlayerEntity serverPlayerEntity) {
        if (FabricLoader.getInstance().isModLoaded("server_translations")) {
            return ServerTranslations.getMessageForPlayer(this.translatableText, serverPlayerEntity);
        }
        return this;
    }
}
