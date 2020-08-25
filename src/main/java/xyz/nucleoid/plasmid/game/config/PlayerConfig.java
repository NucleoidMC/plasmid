package xyz.nucleoid.plasmid.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public final class PlayerConfig {
    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min").forGetter(config -> config.min),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("max").forGetter(config -> config.max),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("threshold").forGetter(config -> Optional.of(config.threshold)),
                Countdown.CODEC.optionalFieldOf("countdown", Countdown.DEFAULT).forGetter(config -> config.countdown)
            ).apply(instance, PlayerConfig::new);
    });

    private final int min;
    private final int max;
    private final int threshold;
    private final Countdown countdown;

    public PlayerConfig(int min, int max, int threshold, Countdown countdown) {
        this.min = min;
        this.max = max;
        this.threshold = threshold;
        this.countdown = countdown;
    }

    public PlayerConfig(int min, int max) {
        this(min, max, min, Countdown.DEFAULT);
    }

    private PlayerConfig(int min, int max, Optional<Integer> threshold, Countdown countdown) {
        this(min, max, threshold.orElse(min), countdown);
    }

    public int getMinPlayers() {
        return this.min;
    }

    public int getMaxPlayers() {
        return this.max;
    }

    public int getThresholdPlayers() {
        return this.threshold;
    }

    public Countdown getCountdown() {
        return this.countdown;
    }

    public static class Countdown {
        public static final Countdown DEFAULT = new Countdown(20 * 60, 20 * 5);

        public static final Codec<Countdown> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.LONG.optionalFieldOf("ready_ticks", DEFAULT.readyTicks).forGetter(countdown -> countdown.readyTicks),
                    Codec.LONG.optionalFieldOf("full_ticks", DEFAULT.fullTicks).forGetter(countdown -> countdown.fullTicks)
            ).apply(instance, Countdown::new);
        });

        private final long readyTicks;
        private final long fullTicks;

        public Countdown(long readyTicks, long fullTicks) {
            this.readyTicks = readyTicks;
            this.fullTicks = fullTicks;
        }

        public long getReadyTicks() {
            return this.readyTicks;
        }

        public long getFullTicks() {
            return this.fullTicks;
        }
    }
}
