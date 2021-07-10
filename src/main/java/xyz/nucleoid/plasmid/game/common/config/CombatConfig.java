package xyz.nucleoid.plasmid.game.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// TODO: allow individual values to be specified?
public final class CombatConfig {
    public static final Codec<CombatConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.BOOL.fieldOf("old_mechanics").forGetter(CombatConfig::isOldMechanics)
        ).apply(instance, CombatConfig::new);
    });

    public static final CombatConfig DEFAULT = new CombatConfig(false);

    private final boolean oldMechanics;

    public CombatConfig(boolean oldMechanics) {
        this.oldMechanics = oldMechanics;
    }

    public boolean isOldMechanics() {
        return this.oldMechanics;
    }
}
