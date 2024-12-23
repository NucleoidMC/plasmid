package xyz.nucleoid.plasmid.impl.portal.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;

public record ActiveGamePortalConfig(Text name,
                                     List<Text> description,
                                     ItemStack icon,
                                     JoinIntent joinIntent,
                                     CustomValuesConfig custom) implements GamePortalConfig {
    public static final MapCodec<ActiveGamePortalConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name", ScreenTexts.EMPTY).forGetter(ActiveGamePortalConfig::name),
            MoreCodecs.listOrUnit(PlasmidCodecs.TEXT).optionalFieldOf("description", List.of()).forGetter(ActiveGamePortalConfig::description),
            MoreCodecs.ITEM_STACK.optionalFieldOf("icon", new ItemStack(Items.GRASS_BLOCK)).forGetter(ActiveGamePortalConfig::icon),
            StringIdentifiable.createCodec(JoinIntent::values).fieldOf("join_intent").forGetter(ActiveGamePortalConfig::joinIntent),
            CustomValuesConfig.CODEC.optionalFieldOf("custom", CustomValuesConfig.empty()).forGetter(ActiveGamePortalConfig::custom)
    ).apply(i, ActiveGamePortalConfig::new));

    @Override
    public MapCodec<ActiveGamePortalConfig> codec() {
        return CODEC;
    }

    public static ActiveGamePortalConfig of(Text name, JoinIntent intent) {
        return new ActiveGamePortalConfig(name, List.of(), ItemStack.EMPTY, intent, CustomValuesConfig.empty());
    }
}
