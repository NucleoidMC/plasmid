package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.menu.*;

/**
 * A loud theme standing in for what Nucleoid Extras provides in production. It keeps a header row and a
 * navigation row, so a menu comes out two rows taller than the region it asked for: a 9:4 menu lands in a
 * 9:6 container and a 9:1 menu in a 9:3 one.
 */
public record TestGameMenuTheme() implements GameMenuTheme {
    public static final MapCodec<TestGameMenuTheme> CODEC = MapCodec.unit(TestGameMenuTheme::new);

    private static final GameMenuInsets INSETS = new GameMenuInsets(1, 1, 0, 0, 1, 0);

    @Override
    public GameMenuInsets baseInsets() {
        return INSETS;
    }

    /**
     * The standard bar, plus a close button split off into the header row.
     */
    @Override
    public GameMenuFeatureLayout featureLayout(GameMenuContext ctx) {
        return GameMenuTheme.super.featureLayout(ctx)
                .with(GameMenuFeatures.CLOSE, 0, 0);
    }

    @Override
    public Component title(GameMenuContext ctx, Component title) {
        return Component.literal("« ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(title.copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" »").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public GuiElement entryElement(GameMenuContext ctx, GameMenuEntry entry) {
        var original = entry.createGuiElement();

        // Carry the original callback over rather than rebuilding one. Hybrid entries bind right-click
        // to a second action that a hand-written callback would silently drop.
        return GuiElementBuilder.from(original.getItemStack().copy())
                .addLoreLine(Component.literal("styled by testmod").withStyle(ChatFormatting.DARK_PURPLE))
                .setCallback(original.getGuiCallback())
                .build();
    }

    @Override
    public GuiElementBuilder chrome(GameMenuContext ctx, GameMenuFeature feature) {
        if (feature == GameMenuFeatures.GAME_FILTER) {
            // Recoloured, but the two states come from the feature itself rather than being hand-rolled.
            boolean active = ctx.isActive(feature);
            var appearance = feature.appearance(active);

            var builder = new GuiElementBuilder(appearance.icon().create())
                    .setItemName(appearance.name().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                    .hideDefaultTooltip();

            if (active) {
                builder.glow();
            }

            return builder;
        }

        if (feature == GameMenuFeatures.BACK) {
            return new GuiElementBuilder(Items.ENDER_PEARL)
                    .setItemName(Component.translatable("gui.back").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .hideDefaultTooltip();
        }

        if (feature == GameMenuFeatures.PREVIOUS_PAGE || feature == GameMenuFeatures.NEXT_PAGE) {
            return new GuiElementBuilder(feature == GameMenuFeatures.NEXT_PAGE ? Items.SPECTRAL_ARROW : Items.ARROW)
                    .setItemName(feature.name().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                    .hideDefaultTooltip();
        }

        return GameMenuTheme.super.chrome(ctx, feature);
    }

    @Override
    public void decorate(GameMenuContext ctx, Layer chrome) {
        var header = new GuiElementBuilder(Items.STAINED_GLASS_PANE.magenta()).hideTooltip().build();
        var nav = new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple()).hideTooltip().build();

        // Read off the resolved frame: the rows kept are always one above and one below the content, but
        // which row that is moves with the container this menu earned.
        var frame = ctx.frame();
        int navRow = frame.y() + frame.height();

        for (int x = 0; x < chrome.getWidth(); x++) {
            chrome.setSlot(x, header);

            if (navRow < chrome.getHeight()) {
                chrome.setSlot(navRow * chrome.getWidth() + x, nav);
            }
        }
    }

    @Override
    public MapCodec<? extends GameMenuTheme> codec() {
        return CODEC;
    }
}
