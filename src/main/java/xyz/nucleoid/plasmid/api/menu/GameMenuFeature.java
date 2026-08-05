package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

/**
 * An optional behaviour a {@link GameMenu} can carry, whose appearance belongs to the active
 * {@link GameMenuTheme}. Plasmid owns what a feature does; the theme owns how it looks and where it sits.
 *
 * <p>A feature carries no position of its own. The theme places it through
 * {@link GameMenuTheme#featureLayout}, so where a control sits is decided once per look rather than by
 * whoever registered the feature.
 *
 * <p>Toggles carry two appearances, and {@link GameMenuContext#isActive} says which applies. Features that
 * are not toggles use the same one for both.
 *
 * @param idle how this looks when not engaged
 * @param active how this looks when engaged
 * @see GameMenuFeatures
 */
public record GameMenuFeature(Appearance idle, Appearance active) {
    public static final Codec<GameMenuFeature> CODEC = PlasmidRegistries.GAME_MENU_FEATURE.byNameCodec();

    /**
     * A feature with a single appearance.
     */
    public GameMenuFeature(Component name, ItemStackTemplate icon) {
        this(new Appearance(name, icon), new Appearance(name, icon));
    }

    public Appearance appearance(boolean active) {
        return active ? this.active : this.idle;
    }

    /**
     * The idle label.
     */
    public Component name() {
        return this.idle.name();
    }

    /**
     * The idle icon.
     */
    public ItemStackTemplate fallbackIcon() {
        return this.idle.icon();
    }

    /**
     * How a feature looks in one state. Used by themes with no artwork of their own.
     */
    public record Appearance(Component name, ItemStackTemplate icon) {
        public static final Codec<Appearance> CODEC = RecordCodecBuilder.create(i -> i.group(
                PlasmidCodecs.TEXT.fieldOf("name").forGetter(Appearance::name),
                ItemStackTemplate.CODEC.fieldOf("icon").forGetter(Appearance::icon)
        ).apply(i, Appearance::new));
    }
}
