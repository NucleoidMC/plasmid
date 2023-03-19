package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;

public record GameMenuEntryConfig(Identifier game,
        Optional<Text> name,
        Optional<List<Text>> description,
        Optional<ItemStack> icon
) implements MenuEntryConfig {

    public static final Codec<GameMenuEntryConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("game").forGetter(GameMenuEntryConfig::game),
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(GameMenuEntryConfig::name),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(GameMenuEntryConfig::description),
                MoreCodecs.ITEM_STACK.optionalFieldOf("icon").forGetter(GameMenuEntryConfig::icon)
        ).apply(instance, GameMenuEntryConfig::new);
    });

    @Override
    public MenuEntry createEntry() {
        var game = new ConcurrentGamePortalBackend(this.game);
        var gameConfig = GameConfigs.get(this.game);

        if (gameConfig == null) {
            return new InvalidMenuEntry(game.getName());
        } else {
            return new GameMenuEntry(
                    game,
                    this.name.orElse(GameConfig.name(gameConfig)),
                    this.description.orElse(gameConfig.description()),
                    this.icon.orElse(gameConfig.icon())
            );
        }
    }

    @Override
    public Codec<? extends MenuEntryConfig> codec() {
        return CODEC;
    }
}