package xyz.nucleoid.plasmid.api.game.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;

import java.util.Optional;

/**
 * A standard configuration type that contains various data used by a {@link GameWaitingLobby}.
 * <p>
 * This involves values such as how many players are required in the game, or how many there can be at a maximum, as
 * well as how long players should wait in the waiting lobby given there being a sufficient number of players.
 *
 * @see GameWaitingLobby
 */
public record WaitingLobbyConfig(PlayerLimiterConfig playerConfig, int minPlayers, int thresholdPlayers, Countdown countdown) {
    public static final MapCodec<WaitingLobbyConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                PlayerLimiterConfig.MAP_CODEC.forGetter(WaitingLobbyConfig::playerConfig),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min").forGetter(WaitingLobbyConfig::minPlayers),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("threshold").forGetter(c -> Optional.of(c.thresholdPlayers)),
                Countdown.CODEC.optionalFieldOf("countdown", Countdown.DEFAULT).forGetter(WaitingLobbyConfig::countdown)
        ).apply(instance, WaitingLobbyConfig::new);
    });

    public static final Codec<WaitingLobbyConfig> CODEC = MAP_CODEC.codec();


    public WaitingLobbyConfig(int min, int max) {
        this(new PlayerLimiterConfig(max), min, min, Countdown.DEFAULT);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private WaitingLobbyConfig(PlayerLimiterConfig playerConfig, int min, Optional<Integer> threshold, Countdown countdown) {
        this(playerConfig, min, threshold.orElse(min), countdown);
    }

    public record Countdown(int readySeconds, int fullSeconds) {
        public static final Countdown DEFAULT = new Countdown(30, 5);

        public static final Codec<Countdown> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.INT.optionalFieldOf("ready_seconds", DEFAULT.readySeconds).forGetter(Countdown::readySeconds),
                    Codec.INT.optionalFieldOf("full_seconds", DEFAULT.fullSeconds).forGetter(Countdown::fullSeconds)
            ).apply(instance, Countdown::new);
        });
    }
}
