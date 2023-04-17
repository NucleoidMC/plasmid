package xyz.nucleoid.plasmid.game.common.team;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;

import java.util.function.Function;

/**
 * A configuration for a {@link GameTeam} containing visual and behavioral settings.
 *
 * @see GameTeam
 */
public final record GameTeamConfig(
        Text name,
        Colors colors,
        boolean friendlyFire,
        boolean indirectFriendlyFire,
        AbstractTeam.CollisionRule collision,
        AbstractTeam.VisibilityRule nameTagVisibility,
        Text prefix,
        Text suffix
) {
    private static final Codec<AbstractTeam.CollisionRule> COLLISION_CODEC = MoreCodecs.stringVariants(AbstractTeam.CollisionRule.values(), rule -> rule.name);
    private static final Codec<AbstractTeam.VisibilityRule> VISIBILITY_CODEC = MoreCodecs.stringVariants(AbstractTeam.VisibilityRule.values(), rule -> rule.name);

    public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                PlasmidCodecs.TEXT.fieldOf("name").forGetter(GameTeamConfig::name),
                Colors.CODEC.optionalFieldOf("color", Colors.NONE).forGetter(GameTeamConfig::colors),
                Codec.BOOL.optionalFieldOf("friendly_fire", true).forGetter(GameTeamConfig::friendlyFire),
                Codec.BOOL.optionalFieldOf("indirect_friendly_fire", true).forGetter(GameTeamConfig::indirectFriendlyFire),
                COLLISION_CODEC.optionalFieldOf("collision", AbstractTeam.CollisionRule.ALWAYS).forGetter(GameTeamConfig::collision),
                VISIBILITY_CODEC.optionalFieldOf("name_tag_visibility", AbstractTeam.VisibilityRule.ALWAYS).forGetter(GameTeamConfig::nameTagVisibility),
                PlasmidCodecs.TEXT.optionalFieldOf("prefix", ScreenTexts.EMPTY).forGetter(GameTeamConfig::prefix),
                PlasmidCodecs.TEXT.optionalFieldOf("suffix", ScreenTexts.EMPTY).forGetter(GameTeamConfig::suffix)
        ).apply(instance, GameTeamConfig::new);
    });

    public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

    public static final GameTeamConfig DEFAULT = GameTeamConfig.builder().build();

    public GameTeamConfig(Text name, Colors colors, boolean friendlyFire, boolean indirectFriendlyFire, AbstractTeam.CollisionRule collision, AbstractTeam.VisibilityRule nameTagVisibility, Text prefix, Text suffix) {
        this.name = name.copy().styled(style -> style.getColor() == null ? style.withColor(colors.chatFormatting()) : style);
        this.colors = colors;
        this.friendlyFire = friendlyFire;
        this.indirectFriendlyFire = indirectFriendlyFire;
        this.collision = collision;
        this.nameTagVisibility = nameTagVisibility;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public GameTeamConfig(Text name, Colors colors, boolean friendlyFire, AbstractTeam.CollisionRule collision, AbstractTeam.VisibilityRule nameTagVisibility, Text prefix, Text suffix) {
        this(name, colors, friendlyFire, false, collision, nameTagVisibility, prefix, suffix);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(GameTeamConfig config) {
        return new Builder(config);
    }

    public ItemStack createFirework(int flight, FireworkRocketItem.Type type) {
        var color = this.fireworkColor().getRgb();
        return ItemStackBuilder.firework(color, flight, type).build();
    }

    public ItemStack applyDye(ItemStack stack) {
        return ItemStackBuilder.of(stack)
                .setDyeColor(this.dyeColor().getRgb())
                .build();
    }

    public Formatting chatFormatting() {
        return this.colors.chatFormatting();
    }

    public TextColor fireworkColor() {
        return this.colors.fireworkColor();
    }

    public DyeColor blockDyeColor() {
        return this.colors.blockDyeColor();
    }

    public TextColor dyeColor() {
        return this.colors.dyeColor();
    }

    public void applyToScoreboard(Team scoreboardTeam) {
        scoreboardTeam.setDisplayName(this.name());
        scoreboardTeam.setColor(this.chatFormatting());
        scoreboardTeam.setFriendlyFireAllowed(this.friendlyFire());
        scoreboardTeam.setCollisionRule(this.collision());
        scoreboardTeam.setNameTagVisibilityRule(this.nameTagVisibility());
        scoreboardTeam.setPrefix(this.prefix());
        scoreboardTeam.setSuffix(this.suffix());
    }

    public static final class Builder {
        private Text name = Text.literal("Team");
        private Colors colors = Colors.NONE;
        private boolean friendlyFire = true;
        private AbstractTeam.CollisionRule collision = AbstractTeam.CollisionRule.ALWAYS;
        private AbstractTeam.VisibilityRule nameTagVisibility = AbstractTeam.VisibilityRule.ALWAYS;
        private Text prefix = ScreenTexts.EMPTY;
        private Text suffix = ScreenTexts.EMPTY;

        Builder() {
        }

        Builder(GameTeamConfig config) {
            this.name = config.name;
            this.colors = config.colors;
            this.friendlyFire = config.friendlyFire;
            this.collision = config.collision;
            this.nameTagVisibility = config.nameTagVisibility;
            this.prefix = config.prefix;
            this.suffix = config.suffix;
        }

        public Builder setName(Text name) {
            this.name = name;
            return this;
        }

        public Builder setColors(Colors colors) {
            this.colors = colors;
            return this;
        }

        public Builder setFriendlyFire(boolean friendlyFire) {
            this.friendlyFire = friendlyFire;
            return this;
        }

        public Builder setCollision(AbstractTeam.CollisionRule collision) {
            this.collision = collision;
            return this;
        }

        public Builder setNameTagVisibility(AbstractTeam.VisibilityRule nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this;
        }

        public Builder setPrefix(Text prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setSuffix(Text suffix) {
            this.suffix = suffix;
            return this;
        }

        public GameTeamConfig build() {
            return new GameTeamConfig(
                    this.name, this.colors,
                    this.friendlyFire, this.collision, this.nameTagVisibility,
                    this.prefix, this.suffix
            );
        }
    }

    public final record Colors(
            Formatting chatFormatting,
            TextColor dyeColor,
            DyeColor blockDyeColor,
            TextColor fireworkColor
    ) {
        private static final Codec<Colors> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Formatting.CODEC.optionalFieldOf("chat", Formatting.RESET).forGetter(Colors::chatFormatting),
                    TextColor.CODEC.fieldOf("dye").forGetter(Colors::dyeColor),
                    DyeColor.CODEC.fieldOf("block_dye").forGetter(Colors::blockDyeColor),
                    TextColor.CODEC.fieldOf("firework").forGetter(Colors::fireworkColor)
            ).apply(instance, Colors::new);
        });

        public static final Codec<Colors> CODEC = Codec.either(DyeColor.CODEC, RECORD_CODEC).xmap(
                either -> either.map(Colors::from, Function.identity()),
                Either::right
        );

        public static final Colors NONE = new Colors(
                Formatting.RESET,
                TextColor.fromFormatting(Formatting.WHITE),
                DyeColor.WHITE,
                TextColor.fromFormatting(Formatting.WHITE)
        );

        public static Colors from(DyeColor dyeColor) {
            var formatting = formatByDye(dyeColor);
            return new Colors(
                    formatting,
                    TextColor.fromRgb(dyeColor(dyeColor)),
                    dyeColor,
                    TextColor.fromRgb(dyeColor.getFireworkColor())
            );
        }

        private static Formatting formatByDye(DyeColor dye) {
            return switch (dye) {
                case WHITE -> Formatting.WHITE;
                case ORANGE -> Formatting.GOLD;
                case MAGENTA, PINK -> Formatting.LIGHT_PURPLE;
                case LIGHT_BLUE -> Formatting.AQUA;
                case YELLOW -> Formatting.YELLOW;
                case LIME -> Formatting.GREEN;
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
