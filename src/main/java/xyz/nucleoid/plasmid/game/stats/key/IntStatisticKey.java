package xyz.nucleoid.plasmid.game.stats.key;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;

public class IntStatisticKey extends StatisticKey<Integer> {
    public IntStatisticKey(Identifier id, StatisticType type) {
        super(id, type);
    }

    @Override
    protected String encodeType() {
        return "int_" + this.type.asString();
    }
}
