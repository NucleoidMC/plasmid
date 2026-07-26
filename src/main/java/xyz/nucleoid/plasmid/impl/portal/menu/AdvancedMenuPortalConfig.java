package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;

public record AdvancedMenuPortalConfig(
        Component name,
        List<Component> description,
        ItemStackTemplate icon,
        List<MenuEntryConfig> entries,
        CustomValuesConfig custom
) implements GamePortalConfig {
    public static final MapCodec<AdvancedMenuPortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(AdvancedMenuPortalConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description", List.of()).forGetter(AdvancedMenuPortalConfig::description),
            ItemStackTemplate.CODEC.optionalFieldOf("icon", new ItemStackTemplate(Items.GRASS_BLOCK)).forGetter(AdvancedMenuPortalConfig::icon),
            MenuEntryConfig.CODEC.listOf().fieldOf("entries").forGetter(AdvancedMenuPortalConfig::entries),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(config -> config.custom)
    ).apply(i, AdvancedMenuPortalConfig::new));

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        Component name;
        if (this.name != null) {
            name = this.name;
        } else {
            name = Component.literal(id.toString());
        }

        return new AdvancedMenuPortalBackend(name, description, icon.create(), this.entries);
    }

    @Override
    public MapCodec<AdvancedMenuPortalConfig> codec() {
        return CODEC;
    }
}
