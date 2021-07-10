package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

/**
 * A simple representation of a team type, containing a name and color.
 */
public final record GameTeam(String display, String key, DyeColor dye, Formatting formatting) {
    public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(GameTeam::key),
                Codec.STRING.fieldOf("display").forGetter(GameTeam::display),
                MoreCodecs.DYE_COLOR.fieldOf("color").forGetter(GameTeam::dye)
        ).apply(instance, GameTeam::new);
    });

    public GameTeam(String key, String display, DyeColor dye) {
        this(key, display, dye, formatByDye(dye));
    }

    public ItemStack createFirework(int flight, FireworkItem.Type type) {
        return ItemStackBuilder.firework(this.fireworkColor(), flight, type)
                .build();
    }

    public ItemStack dye(ItemStack stack) {
        return ItemStackBuilder.of(stack)
                .setDyeColor(this.color())
                .build();
    }

    public int color() {
        var components = this.dye.getColorComponents();
        int red = MathHelper.floor(components[0] * 255.0F) & 0xFF;
        int green = MathHelper.floor(components[1] * 255.0F) & 0xFF;
        int blue = MathHelper.floor(components[2] * 255.0F) & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    public int fireworkColor() {
        return this.dye.getFireworkColor();
    }

    private static Formatting formatByDye(DyeColor dye) {
        return switch (dye) {
            case WHITE -> Formatting.WHITE;
            case ORANGE -> Formatting.GOLD;
            case MAGENTA -> Formatting.LIGHT_PURPLE;
            case LIGHT_BLUE -> Formatting.AQUA;
            case YELLOW -> Formatting.YELLOW;
            case LIME -> Formatting.GREEN;
            case PINK -> Formatting.LIGHT_PURPLE;
            case GRAY -> Formatting.DARK_GRAY;
            case LIGHT_GRAY -> Formatting.GRAY;
            case CYAN -> Formatting.DARK_AQUA;
            case PURPLE -> Formatting.DARK_PURPLE;
            case BLUE -> Formatting.BLUE;
            case BROWN -> Formatting.DARK_RED;
            case GREEN -> Formatting.DARK_GREEN;
            case RED -> Formatting.RED;
            case BLACK -> Formatting.BLACK;
        };
    }
}
