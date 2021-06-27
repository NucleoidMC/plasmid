package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import xyz.nucleoid.plasmid.game.stats.key.DoubleStatisticKey;
import xyz.nucleoid.plasmid.game.stats.key.FloatStatisticKey;
import xyz.nucleoid.plasmid.game.stats.key.IntStatisticKey;

/**
 * Type-safe keys for identifying statistics.
 * <p>
 * Ideally should have a corresponding translation key in the form <code>statistic.[bundle namespace].[key namespace].[key path]</code>
 * or <code>statistic.[key namespace].[key path]</code> for future-proofing
 *
 * @param <T> The type of {@link Number} this key stores
 */
public abstract class StatisticKey<T extends Number> {
    protected final Identifier id;
    protected final StatisticType type;

    protected StatisticKey(Identifier id, StatisticType type) {
        this.id = id;
        this.type = type;
    }

    public Identifier getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public JsonObject encodeValueUnchecked(Number value) {
        return this.encodeValue((T) value);
    }

    public JsonObject encodeValue(T value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", this.encodeType());
        obj.addProperty("value", value);
        return obj;
    }

    protected abstract String encodeType();

    public static StatisticKey<Integer> intKey(Identifier id, StatisticType type) {
        return new IntStatisticKey(id, type);
    }

    public static StatisticKey<Float> floatKey(Identifier id, StatisticType type) {
        return new FloatStatisticKey(id, type);
    }

    public static StatisticKey<Double> doubleKey(Identifier id, StatisticType type) {
        return new DoubleStatisticKey(id, type);
    }

    public enum StatisticType implements StringIdentifiable {
        TOTAL("total"),
        ROLLING_AVERAGE("rolling_average"),
        ;
        private final String name;

        StatisticType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
