package xyz.nucleoid.plasmid.impl.menu;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.menu.GameMenuContext;
import xyz.nucleoid.plasmid.api.menu.GameMenuFeatureLayout;
import xyz.nucleoid.plasmid.api.menu.GameMenuInsets;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;

import java.util.Optional;

/**
 * The theme used when a server has configured none: a plain container with the content in the middle and a
 * filler strip over whatever rows and columns it keeps around it, carrying the navigation controls.
 *
 * <p>Entirely configurable, so a datapack can change the look without code. Nothing here fixes a
 * container size. The menu asks for a region and this adds its strip around it, so one definition dresses a
 * two-row menu and a five-row one.
 *
 * @param features where the feature controls sit, replacing the default bar outright rather than adding to
 * it, so a datapack that names any control names them all
 */
public record SimpleGameMenuTheme(
        GameMenuInsets insets,
        Optional<ItemStackTemplate> filler,
        Optional<GameMenuFeatureLayout> features
) implements GameMenuTheme {
    public static final MapCodec<SimpleGameMenuTheme> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameMenuInsets.CODEC.forGetter(SimpleGameMenuTheme::insets),
            ItemStackTemplate.CODEC.optionalFieldOf("filler").forGetter(SimpleGameMenuTheme::filler),
            GameMenuFeatureLayout.CODEC.optionalFieldOf("features").forGetter(SimpleGameMenuTheme::features)
    ).apply(i, SimpleGameMenuTheme::new));

    /**
     * Used when no datapack provides {@code plasmid:default}.
     */
    public static final SimpleGameMenuTheme DEFAULT = new SimpleGameMenuTheme(
            new GameMenuInsets(0, 1, 0, 0, 1, 0),
            Optional.empty(),
            Optional.empty()
    );

    @Override
    public GameMenuInsets baseInsets() {
        return this.insets;
    }

    @Override
    public GameMenuFeatureLayout featureLayout(GameMenuContext ctx) {
        return this.features.orElseGet(() -> GameMenuTheme.super.featureLayout(ctx));
    }

    /**
     * Fills every slot the content does not occupy, so any inset reads as a deliberate border.
     */
    @Override
    public void decorate(GameMenuContext ctx, Layer chrome) {
        var frame = ctx.frame();

        var stack = this.filler.map(ItemStackTemplate::create).orElseGet(() -> Items.STAINED_GLASS_PANE.gray().getDefaultInstance());
        var element = new GuiElementBuilder(stack.copy()).hideTooltip().build();

        for (int y = 0; y < chrome.getHeight(); y++) {
            for (int x = 0; x < chrome.getWidth(); x++) {
                if (!frame.contains(x - frame.x(), y - frame.y())) {
                    chrome.setSlot(x + y * chrome.getWidth(), element);
                }
            }
        }
    }

    @Override
    public MapCodec<? extends GameMenuTheme> codec() {
        return CODEC;
    }
}
