package xyz.nucleoid.plasmid.game.stats.key;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;

public class DoubleStatisticKey extends StatisticKey<Double> {
    public DoubleStatisticKey(Identifier id, StatisticType type) {
        super(id, type);
    }

    @Override
    protected String encodeType() {
        return "float_" + this.type.asString();
    }
}
