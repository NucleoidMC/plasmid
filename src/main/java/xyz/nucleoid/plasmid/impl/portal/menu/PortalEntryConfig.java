package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.impl.portal.GamePortalManager;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PortalEntryConfig(
        Identifier portal,
        Optional<Component> name,
        Optional<List<Component>> description,
        Optional<ItemStackTemplate> icon
) implements MenuEntryConfig {

    public static final MapCodec<PortalEntryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("portal").forGetter(PortalEntryConfig::portal),
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(PortalEntryConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description").forGetter(PortalEntryConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(PortalEntryConfig::icon)
    ).apply(i, PortalEntryConfig::new));

    @Override
    public MenuEntry createEntry() {
        var portal = GamePortalManager.INSTANCE.byId(this.portal);

        if (portal != null) {
            return new PortalEntry(
                    portal,
                    this.name.orElse(portal.getName()),
                    this.description.orElse(portal.getDescription()),
                    this.icon.map(ItemStackTemplate::create).orElse(portal.getIcon())
            );
        }

        return new InvalidMenuEntry(this.name);
    }

    @Override
    public MapCodec<PortalEntryConfig> codec() {
        return CODEC;
    }
}
