package xyz.nucleoid.plasmid.game.portal.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.game.config.CustomValuesConfig;

import java.util.List;
import java.util.Optional;

public final class MenuPortalConfig implements GamePortalConfig {
    public static final Codec<MenuPortalConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.optionalFieldOf("translation").forGetter(config -> Optional.ofNullable(config.translation)),
                Entry.CODEC.listOf().fieldOf("games").forGetter(config -> config.games),
                CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(config -> config.custom)
        ).apply(instance, MenuPortalConfig::new);
    });

    private final String translation;
    private final List<Entry> games;
    private final CustomValuesConfig custom;

    MenuPortalConfig(Optional<String> translation, List<Entry> games, CustomValuesConfig custom) {
        this.translation = translation.orElse(null);
        this.games = games;
        this.custom = custom;
    }

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        Text name;
        if (this.translation != null) {
            name = new TranslatableText(this.translation);
        } else {
            name = new LiteralText(id.toString());
        }

        return new MenuPortalBackend(name, this.games);
    }

    @Override
    public CustomValuesConfig getCustom() {
        return this.custom;
    }

    @Override
    public Codec<? extends GamePortalConfig> getCodec() {
        return CODEC;
    }

    public record Entry(Identifier game, ItemStack icon) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Identifier.CODEC.fieldOf("game").forGetter(entry -> entry.game),
                    MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Blocks.WHITE_WOOL)).forGetter(entry -> entry.icon)
            ).apply(instance, Entry::new);
        });
    }
}
