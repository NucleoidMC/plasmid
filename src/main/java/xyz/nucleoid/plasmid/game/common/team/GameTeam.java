package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.function.Function;

/**
 * A simple representation of a team type, containing a name and color.
 */
public final record GameTeam(String key, MutableText display, Colors colors) {
    public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(GameTeam::key),
                MoreCodecs.TEXT.fieldOf("display").forGetter(GameTeam::display),
                Colors.CODEC.fieldOf("color").forGetter(GameTeam::colors)
        ).apply(instance, GameTeam::new);
    });

    public GameTeam(String key, MutableText display, Colors colors) {
        this.key = key;
        this.colors = colors;
        this.display = display.styled(style -> style.getColor() == null ? style.withColor(colors.color()) : style);
    }

    public GameTeam(String key, MutableText display, DyeColor color) {
        this(key, display, Colors.from(color));
    }

    public ItemStack createFirework(int flight, FireworkItem.Type type) {
        var color = this.fireworkColor().getRgb();
        return ItemStackBuilder.firework(color, flight, type).build();
    }

    public ItemStack dye(ItemStack stack) {
        return ItemStackBuilder.of(stack)
                .setDyeColor(this.dyeColor().getRgb())
                .build();
    }

    public TextColor color() {
        return this.colors.color();
    }

    public TextColor fireworkColor() {
        return this.colors.fireworkColor();
    }

    public Formatting formatting() {
        return this.colors.formatting();
    }

    public DyeColor blockDyeColor() {
        return this.colors.blockDyeColor();
    }

    public TextColor dyeColor() {
        return this.colors.dyeColor();
    }

    public final record Colors(
            TextColor color,
            TextColor dyeColor,
            DyeColor blockDyeColor,
            TextColor fireworkColor,
            Formatting formatting
    ) {
        private static final Codec<Colors> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    MoreCodecs.TEXT_COLOR.fieldOf("chat").forGetter(Colors::color),
                    MoreCodecs.TEXT_COLOR.fieldOf("dye").forGetter(Colors::dyeColor),
                    MoreCodecs.DYE_COLOR.fieldOf("block_dye").forGetter(Colors::blockDyeColor),
                    MoreCodecs.TEXT_COLOR.fieldOf("firework").forGetter(Colors::fireworkColor),
                    MoreCodecs.FORMATTING.optionalFieldOf("formatting", Formatting.RESET).forGetter(Colors::formatting)
            ).apply(instance, Colors::new);
        });

        public static final Codec<Colors> CODEC = Codec.either(MoreCodecs.DYE_COLOR, RECORD_CODEC).xmap(
                either -> either.map(Colors::from, Function.identity()),
                Either::right
        );

        public static Colors from(DyeColor dyeColor) {
            var formatting = formatByDye(dyeColor);

            return new Colors(
                    TextColor.fromFormatting(formatting),
                    TextColor.fromRgb(dyeColor(dyeColor)),
                    dyeColor,
                    TextColor.fromRgb(dyeColor.getFireworkColor()),
                    formatting
            );
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

        private static int dyeColor(DyeColor dye) {
            var components = dye.getColorComponents();
            int red = MathHelper.floor(components[0] * 255.0F) & 0xFF;
            int green = MathHelper.floor(components[1] * 255.0F) & 0xFF;
            int blue = MathHelper.floor(components[2] * 255.0F) & 0xFF;
            return (red << 16) | (green << 8) | blue;
        }
    }
}
