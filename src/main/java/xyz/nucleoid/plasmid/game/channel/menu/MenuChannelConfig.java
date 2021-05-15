package xyz.nucleoid.plasmid.game.channel.menu;

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
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelConfig;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;

import java.util.List;
import java.util.Optional;

public final class MenuChannelConfig implements GameChannelConfig {
    public static final Codec<MenuChannelConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.optionalFieldOf("translation").forGetter(config -> Optional.ofNullable(config.translation)),
                Entry.CODEC.listOf().fieldOf("games").forGetter(config -> config.games)
        ).apply(instance, MenuChannelConfig::new);
    });

    private final String translation;
    private final List<Entry> games;

    MenuChannelConfig(Optional<String> translation, List<Entry> games) {
        this.translation = translation.orElse(null);
        this.games = games;
    }

    @Override
    public GameChannelBackend createBackend(MinecraftServer server, Identifier id, GameChannelMembers members) {
        Text name;
        if (this.translation != null) {
            name = new TranslatableText(this.translation);
        } else {
            name = new LiteralText(id.toString());
        }

        return new MenuChannelBackend(name, this.games, members);
    }

    @Override
    public Codec<? extends GameChannelConfig> getCodec() {
        return CODEC;
    }

    public static class Entry {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Identifier.CODEC.fieldOf("game").forGetter(entry -> entry.game),
                    MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Blocks.WHITE_WOOL)).forGetter(entry -> entry.icon)
            ).apply(instance, Entry::new);
        });

        public final Identifier game;
        public final ItemStack icon;

        Entry(Identifier game, ItemStack icon) {
            this.game = game;
            this.icon = icon;
        }
    }
}
