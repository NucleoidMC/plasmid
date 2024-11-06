package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;

public record AdvancedMenuPortalConfig(
        Text name,
        List<Text> description,
        ItemStack icon,
        List<MenuEntryConfig> entries,
        CustomValuesConfig custom
) implements GamePortalConfig {
    public static final MapCodec<AdvancedMenuPortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name", ScreenTexts.EMPTY).forGetter(AdvancedMenuPortalConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description", List.of()).forGetter(AdvancedMenuPortalConfig::description),
            MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(AdvancedMenuPortalConfig::icon),
            MenuEntryConfig.CODEC.listOf().fieldOf("entries").forGetter(AdvancedMenuPortalConfig::entries),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(config -> config.custom)
    ).apply(i, AdvancedMenuPortalConfig::new));

    @Override
    public GamePortalBackend createBackend(MinecraftServer server, Identifier id) {
        Text name;
        if (this.name != null) {
            name = this.name;
        } else {
            name = Text.literal(id.toString());
        }

        return new AdvancedMenuPortalBackend(name, description, icon, this.entries);
    }

    @Override
    public MapCodec<AdvancedMenuPortalConfig> codec() {
        return CODEC;
    }
}
