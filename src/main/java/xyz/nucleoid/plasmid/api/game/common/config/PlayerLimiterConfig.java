package xyz.nucleoid.plasmid.api.game.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.api.game.common.PlayerLimiter;

import java.util.OptionalInt;

/**
 * A standard configuration type that contains various data used by a {@link PlayerLimiter}.
 * <p>
 * This involves values such as how many players can join the game and whatever spectators are allowed.
 *
 * @see PlayerLimiter
 */
public record PlayerLimiterConfig(OptionalInt maxPlayers, boolean allowSpectators) {
    public static final MapCodec<PlayerLimiterConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).xmap(OptionalInt::of, OptionalInt::getAsInt).optionalFieldOf("max", OptionalInt.empty()).forGetter(PlayerLimiterConfig::maxPlayers),
                Codec.BOOL.optionalFieldOf("spectators", true).forGetter(PlayerLimiterConfig::allowSpectators)
        ).apply(instance, PlayerLimiterConfig::new);
    });

    public static final Codec<PlayerLimiterConfig> CODEC = MAP_CODEC.codec();


    public PlayerLimiterConfig(int max) {
        this(OptionalInt.of(max), true);
    }

    public PlayerLimiterConfig() {
        this(OptionalInt.empty(), true);
    }
}
