package xyz.nucleoid.plasmid.api.game.common.team;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.scores.TeamColor;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

/**
 * A configuration for a {@link GameTeam} containing visual and behavioral settings.
 *
 * @see GameTeam
 */
public final record GameTeamConfig(
        Component name,
        Colors colors,
        boolean friendlyFire,
        Team.CollisionRule collision,
        Team.Visibility nameTagVisibility,
        Component prefix,
        Component suffix
) {
    private static final Codec<Team.CollisionRule> COLLISION_CODEC = MoreCodecs.stringVariants(Team.CollisionRule.values(), rule -> rule.name);
    private static final Codec<Team.Visibility> VISIBILITY_CODEC = MoreCodecs.stringVariants(Team.Visibility.values(), rule -> rule.name);

    public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(config -> Optional.of(config.name)),
                Colors.CODEC.optionalFieldOf("color", Colors.NONE).forGetter(GameTeamConfig::colors),
                Codec.BOOL.optionalFieldOf("friendly_fire", true).forGetter(GameTeamConfig::friendlyFire),
                COLLISION_CODEC.optionalFieldOf("collision", Team.CollisionRule.ALWAYS).forGetter(GameTeamConfig::collision),
                VISIBILITY_CODEC.optionalFieldOf("name_tag_visibility", Team.Visibility.ALWAYS).forGetter(GameTeamConfig::nameTagVisibility),
                PlasmidCodecs.TEXT.optionalFieldOf("prefix", CommonComponents.EMPTY).forGetter(GameTeamConfig::prefix),
                PlasmidCodecs.TEXT.optionalFieldOf("suffix", CommonComponents.EMPTY).forGetter(GameTeamConfig::suffix)
        ).apply(instance, GameTeamConfig::of);
    });

    public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

    public static final GameTeamConfig DEFAULT = GameTeamConfig.builder().build();

    public GameTeamConfig(Component name, Colors colors, boolean friendlyFire, Team.CollisionRule collision, Team.Visibility nameTagVisibility, Component prefix, Component suffix) {
        this.name = name.copy().withStyle(style -> style.getColor() == null && colors.teamColor.isPresent() ? style.withColor(colors.teamColor.get().textColor()) : style);
        this.colors = colors;
        this.friendlyFire = friendlyFire;
        this.collision = collision;
        this.nameTagVisibility = nameTagVisibility;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(GameTeamConfig config) {
        return new Builder(config);
    }

    public ItemStack createFirework(int flight, FireworkExplosion.Shape type) {
        var color = this.fireworkColor().getValue();
        return ItemStackBuilder.firework(color, flight, type).build();
    }

    public ItemStack applyDye(ItemStack stack) {
        return ItemStackBuilder.of(stack)
                .setDyeColor(this.dyeColor().getValue())
                .build();
    }

    @Deprecated
    public ChatFormatting chatFormatting() {
        return this.colors.chatFormatting();
    }

    public Optional<TeamColor> teamColor() {
        return this.colors.teamColor();
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

    public void applyToScoreboard(PlayerTeam scoreboardTeam) {
        scoreboardTeam.setDisplayName(this.name());
        scoreboardTeam.setColor(this.teamColor());
        scoreboardTeam.setAllowFriendlyFire(this.friendlyFire());
        scoreboardTeam.setCollisionRule(this.collision());
        scoreboardTeam.setNameTagVisibility(this.nameTagVisibility());
        scoreboardTeam.setPlayerPrefix(this.prefix());
        scoreboardTeam.setPlayerSuffix(this.suffix());
    }

    public static GameTeamConfig of(Optional<Component> name, Colors colors, boolean friendlyFire, Team.CollisionRule collision, Team.Visibility nameTagVisibility, Component prefix, Component suffix) {
        return new GameTeamConfig(getNameWithColorFallback(name, colors), colors, friendlyFire, collision, nameTagVisibility, prefix, suffix);
    }

    private static Component getNameWithColorFallback(Optional<Component> name, Colors colors) {
        return name.orElseGet(() -> {
            if (colors == Colors.NONE) {
                return Component.literal("Team");
            } else {
                return Component.translatable("color.minecraft." + colors.blockDyeColor().getName());
            }
        });
    }

    public static final class Builder {
        private Optional<Component> name = Optional.empty();
        private Colors colors = Colors.NONE;
        private boolean friendlyFire = true;
        private Team.CollisionRule collision = Team.CollisionRule.ALWAYS;
        private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
        private Component prefix = CommonComponents.EMPTY;
        private Component suffix = CommonComponents.EMPTY;

        Builder() {
        }

        Builder(GameTeamConfig config) {
            this.name = Optional.of(config.name);
            this.colors = config.colors;
            this.friendlyFire = config.friendlyFire;
            this.collision = config.collision;
            this.nameTagVisibility = config.nameTagVisibility;
            this.prefix = config.prefix;
            this.suffix = config.suffix;
        }

        public Builder setName(Component name) {
            this.name = Optional.of(name);
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

        public Builder setCollision(Team.CollisionRule collision) {
            this.collision = collision;
            return this;
        }

        public Builder setNameTagVisibility(Team.Visibility nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this;
        }

        public Builder setPrefix(Component prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setSuffix(Component suffix) {
            this.suffix = suffix;
            return this;
        }

        public GameTeamConfig build() {
            return GameTeamConfig.of(
                    this.name, this.colors,
                    this.friendlyFire, this.collision, this.nameTagVisibility,
                    this.prefix, this.suffix
            );
        }
    }

    public final record Colors(
            Optional<TeamColor> teamColor,
            TextColor dyeColor,
            DyeColor blockDyeColor,
            TextColor fireworkColor
    ) {
        private static final Codec<Colors> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    TeamColor.CODEC.optionalFieldOf("chat").forGetter(Colors::teamColor),
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
                Optional.empty(),
                TextColor.fromLegacyFormat(ChatFormatting.WHITE),
                DyeColor.WHITE,
                TextColor.fromLegacyFormat(ChatFormatting.WHITE)
        );

        @Deprecated
        public Colors(
                ChatFormatting chatFormatting,
                TextColor dyeColor,
                DyeColor blockDyeColor,
                TextColor fireworkColor
        ) {
            this(Optional.ofNullable(switch (chatFormatting) {
                case WHITE -> TeamColor.WHITE;
                case GOLD -> TeamColor.GOLD;
                case LIGHT_PURPLE -> TeamColor.LIGHT_PURPLE;
                case AQUA -> TeamColor.AQUA;
                case YELLOW -> TeamColor.YELLOW;
                case GREEN -> TeamColor.GREEN;
                case DARK_GRAY -> TeamColor.DARK_GRAY;
                case GRAY -> TeamColor.GRAY;
                case DARK_AQUA -> TeamColor.DARK_AQUA;
                case DARK_PURPLE -> TeamColor.DARK_PURPLE;
                case BLUE -> TeamColor.BLUE;
                case DARK_RED -> TeamColor.DARK_RED;
                case DARK_BLUE -> TeamColor.DARK_BLUE;
                case DARK_GREEN -> TeamColor.DARK_GREEN;
                case RED -> TeamColor.RED;
                case BLACK -> TeamColor.BLACK;
                default -> null;
            }), dyeColor, blockDyeColor, fireworkColor);
        }

        public static Colors from(DyeColor dyeColor) {
            var formatting = formatByDye(dyeColor);
            return new Colors(
                    Optional.of(formatting),
                    TextColor.fromRgb(dyeColor.getTextureDiffuseColor()),
                    dyeColor,
                    TextColor.fromRgb(dyeColor.getFireworkColor())
            );
        }

        @Deprecated
        public ChatFormatting chatFormatting() {
            return switch (this.teamColor.orElse(null)) {
                case WHITE -> ChatFormatting.WHITE;
                case GOLD -> ChatFormatting.GOLD;
                case LIGHT_PURPLE -> ChatFormatting.LIGHT_PURPLE;
                case AQUA -> ChatFormatting.AQUA;
                case YELLOW -> ChatFormatting.YELLOW;
                case GREEN -> ChatFormatting.GREEN;
                case DARK_GRAY -> ChatFormatting.DARK_GRAY;
                case GRAY -> ChatFormatting.GRAY;
                case DARK_AQUA -> ChatFormatting.DARK_AQUA;
                case DARK_PURPLE -> ChatFormatting.DARK_PURPLE;
                case BLUE -> ChatFormatting.BLUE;
                case DARK_RED -> ChatFormatting.DARK_RED;
                case DARK_BLUE -> ChatFormatting.DARK_BLUE;
                case DARK_GREEN -> ChatFormatting.DARK_GREEN;
                case RED -> ChatFormatting.RED;
                case BLACK -> ChatFormatting.BLACK;
                case null -> ChatFormatting.RESET;
            };
        }

        private static TeamColor formatByDye(DyeColor dye) {
            return switch (dye) {
                case WHITE -> TeamColor.WHITE;
                case ORANGE -> TeamColor.GOLD;
                case MAGENTA, PINK -> TeamColor.LIGHT_PURPLE;
                case LIGHT_BLUE -> TeamColor.AQUA;
                case YELLOW -> TeamColor.YELLOW;
                case LIME -> TeamColor.GREEN;
                case GRAY -> TeamColor.DARK_GRAY;
                case LIGHT_GRAY -> TeamColor.GRAY;
                case CYAN -> TeamColor.DARK_AQUA;
                case PURPLE -> TeamColor.DARK_PURPLE;
                case BLUE -> TeamColor.BLUE;
                case BROWN -> TeamColor.DARK_RED;
                case GREEN -> TeamColor.DARK_GREEN;
                case RED -> TeamColor.RED;
                case BLACK -> TeamColor.BLACK;
            };
        }
    }
}
