package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.game.portal.GamePortalManager;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;

public record PortalGuiEntryConfig(
        Identifier portal,
        Optional<Text> name,
        Optional<List<Text>> description,
        Optional<ItemStack> icon
) implements MenuEntryConfig {

    public static final Codec<PortalGuiEntryConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("portal").forGetter(PortalGuiEntryConfig::portal),
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(PortalGuiEntryConfig::name),
                MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(PortalGuiEntryConfig::description),
                MoreCodecs.ITEM_STACK.optionalFieldOf("icon").forGetter(PortalGuiEntryConfig::icon)
        ).apply(instance, PortalGuiEntryConfig::new);
    });

    @Override
    public MenuEntry createEntry() {
        var portal = GamePortalManager.INSTANCE.byId(this.portal);

        if (portal != null) {
            var provider = portal.getGuiProvider();
            if (provider != null) {
                return new PortalGuiEntry(
                        portal,
                        provider,
                        this.name.orElse(portal.getName()),
                        this.description.orElse(portal.getDescription()),
                        this.icon.orElse(portal.getIcon())
                );
            }
        }

        return new InvalidMenuEntry(this.name);
    }

    @Override
    public Codec<? extends MenuEntryConfig> codec() {
        return CODEC;
    }
}