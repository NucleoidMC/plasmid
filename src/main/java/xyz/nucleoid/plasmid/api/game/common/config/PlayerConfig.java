package xyz.nucleoid.plasmid.api.game.common.config;

import com.mojang.serialization.Codec;
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
public record PlayerConfig(int minPlayers, int maxPlayers, int thresholdPlayers, Countdown countdown) {
    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min").forGetter(PlayerConfig::minPlayers),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("max").forGetter(PlayerConfig::maxPlayers),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("threshold").forGetter(c -> Optional.of(c.thresholdPlayers)),
                Countdown.CODEC.optionalFieldOf("countdown", Countdown.DEFAULT).forGetter(PlayerConfig::countdown)
        ).apply(instance, PlayerConfig::new);
    });

    public PlayerConfig(int min, int max) {
        this(min, max, min, Countdown.DEFAULT);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private PlayerConfig(int min, int max, Optional<Integer> threshold, Countdown countdown) {
        this(min, max, threshold.orElse(min), countdown);
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
