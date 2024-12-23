package xyz.nucleoid.plasmid.impl.portal.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

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
    public MapCodec<AdvancedMenuPortalConfig> codec() {
        return CODEC;
    }
}
