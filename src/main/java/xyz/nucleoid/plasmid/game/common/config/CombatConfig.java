package xyz.nucleoid.plasmid.game.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// TODO: allow individual values to be specified?
public record CombatConfig(boolean oldMechanics) {
    public static final Codec<CombatConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.BOOL.fieldOf("old_mechanics").forGetter(CombatConfig::oldMechanics)
        ).apply(instance, CombatConfig::new)
    );

    public static final CombatConfig DEFAULT = new CombatConfig(false);
}
