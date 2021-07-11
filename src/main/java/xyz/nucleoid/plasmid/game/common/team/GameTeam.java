package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

/**
 * A simple representation of a team type, containing a name and color.
 */
public final class GameTeam {
    public static final Codec<GameTeam> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(GameTeam::key),
                MoreCodecs.TEXT.fieldOf("display").forGetter(GameTeam::display),
                Codec.either(MoreCodecs.DYE_COLOR, TeamColorData.CODEC).fieldOf("color").forGetter((team) -> Either.right(new TeamColorData(team.color, team.dyeColor, team.blockDyeColor, team.fireworkColor, team.formatting)))

        ).apply(instance, GameTeam::new);
    });

    private final String key;
    private final MutableText display;
    private final TextColor color;
    private final TextColor dyeColor;
    private final TextColor fireworkColor;
    private final Formatting formatting;
    private final DyeColor blockDyeColor;

    public GameTeam(String key, MutableText display, DyeColor color) {
        this(key, display, Either.left(color));
    }

    public GameTeam(String key, MutableText display, TeamColorData color) {
        this(key, display, Either.right(color));
    }

    public GameTeam(String key, MutableText display, Either<DyeColor, TeamColorData> color) {
        this.key = key;

        if (color.left().isPresent()) {
            DyeColor dyeColor = color.left().get();
            this.formatting = formatByDye(dyeColor);
            this.blockDyeColor = dyeColor;
            this.color = TextColor.fromFormatting(this.formatting);
            this.dyeColor = TextColor.fromRgb(dyeColor(dyeColor));
            this.fireworkColor = TextColor.fromRgb(dyeColor.getFireworkColor());
        } else {
            TeamColorData colorData = color.right().get();
            this.formatting = colorData.formatting();
            this.color = colorData.color;
            this.dyeColor = colorData.dyeColor;
            this.blockDyeColor = colorData.blockDyeColor;
            this.fireworkColor = colorData.fireworkColor;
        }

        this.display = display.styled((style) -> style.getColor() == null ? style.withColor(this.color) : style);
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

    public ItemStack createFirework(int flight, FireworkItem.Type type) {
        return ItemStackBuilder.firework(this.fireworkColor(), flight, type)
                .build();
    }

    public String key() {
        return this.key;
    }

    public MutableText display() {
        return this.display;
    }

    public ItemStack dye(ItemStack stack) {
        return ItemStackBuilder.of(stack)
                .setDyeColor(this.dyeColor.getRgb())
                .build();
    }

    public TextColor color() {
        return this.color;
    }

    public int fireworkColor() {
        return this.fireworkColor.getRgb();
    }

    public Formatting formatting() {
        return this.formatting;
    }

    public DyeColor blockDye() {
        return this.blockDyeColor;
    }

    public TextColor dye() {
        return this.dyeColor;
    }

    public record TeamColorData(TextColor color, TextColor dyeColor, DyeColor blockDyeColor, TextColor fireworkColor, Formatting formatting) {
        public static final Codec<TeamColorData> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.STRING.fieldOf("chat").forGetter((t) -> t.color.toString()),
                    Codec.STRING.fieldOf("dye").forGetter((t) -> t.dyeColor.toString()),
                    MoreCodecs.DYE_COLOR.fieldOf("block_dye").forGetter((t) -> t.blockDyeColor),
                    Codec.STRING.fieldOf("firework").forGetter((t) -> t.fireworkColor.toString()),
                    MoreCodecs.FORMATTING.fieldOf("formatting").forGetter((t) -> t.formatting)
            ).apply(instance, TeamColorData::new);
        });

        public TeamColorData(String color, String dyeColor, DyeColor blockDyeColor, String fireworkColor, Formatting formatting) {
            this(TextColor.parse(color), TextColor.parse(dyeColor), blockDyeColor, TextColor.parse(fireworkColor), formatting);
        }
    }
}
