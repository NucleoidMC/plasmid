package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * How a {@link GameMenuTheme} dresses the things it draws: the name, the lore beneath it, and the tooltip
 * panel behind both.
 *
 * <p>One of these covers entries and another covers feature controls, so a theme can mark them apart while
 * keeping one tooltip look across the menu.
 *
 * @param tooltipStyle a {@code minecraft:tooltip_style} sprite, from
 * {@code textures/gui/sprites/tooltip/<name>_{background,frame}.png}
 */
public record GameMenuElementStyle(
        GameMenuTextStyle name,
        GameMenuTextStyle lore,
        Optional<Identifier> tooltipStyle
) {
    public static final GameMenuElementStyle NONE = new GameMenuElementStyle(GameMenuTextStyle.NONE, GameMenuTextStyle.NONE, Optional.empty());

    public static final MapCodec<GameMenuElementStyle> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuTextStyle.CODEC.optionalFieldOf("name", GameMenuTextStyle.NONE).forGetter(GameMenuElementStyle::name),
            GameMenuTextStyle.CODEC.optionalFieldOf("lore", GameMenuTextStyle.NONE).forGetter(GameMenuElementStyle::lore),
            Identifier.CODEC.optionalFieldOf("tooltip_style").forGetter(GameMenuElementStyle::tooltipStyle)
    ).apply(i, GameMenuElementStyle::new));

    public static final Codec<GameMenuElementStyle> CODEC = MAP_CODEC.codec();

    /**
     * A builder over {@code icon}, named and panelled by this style. The default tooltip is hidden, since a
     * theme writing its own lore does not want the item's on top of it.
     */
    public GuiElementBuilder element(ItemStack icon, Component name) {
        var builder = GuiElementBuilder.from(icon)
                .hideDefaultTooltip()
                .setItemName(this.name.apply(name));

        this.tooltipStyle.ifPresent(style -> builder.setComponent(DataComponents.TOOLTIP_STYLE, style));

        return builder;
    }

    /**
     * Adds one lore line in this style.
     */
    public void addLore(GuiElementBuilder builder, Component line) {
        builder.addLoreLine(this.lore.apply(line));
    }
}
