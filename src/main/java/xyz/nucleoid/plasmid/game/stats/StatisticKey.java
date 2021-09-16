package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

/**
 * Type-safe keys for identifying statistics.
 * <p>
 * Should have a corresponding translation key in the form <code>statistic.[key namespace].[key path]</code>
 * <p>
 * See {@link StatisticKeys} for some general keys for minigames to make use of.
 *
 * @param <T> The type of {@link Number} this key stores
 * @see StatisticKeys
 */
public record StatisticKey<T extends Number>(
        Identifier id,
        ValueType valueType,
        StorageType storageType,
        boolean hidden
) {
    public StatisticKey {
        StatisticKey.validateKeyId(id);
    }

    @SuppressWarnings("unchecked")
    public JsonObject encodeValueUnchecked(Number value) {
        return this.encodeValue((T) value);
    }

    public JsonObject encodeValue(T value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", this.encodeType());
        obj.addProperty("value", value);
        obj.addProperty("hidden", this.hidden);
        return obj;
    }

    private String encodeType() {
        return this.valueType.asString() + "_" + this.storageType.asString();
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("statistic", this.id);
    }

    public static StatisticKey<Integer> intKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.INT, storageType, false);
    }

    public static StatisticKey<Float> floatKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType, false);
    }

    public static StatisticKey<Double> doubleKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType, false);
    }

    public static StatisticKey<Integer> hiddenIntKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.INT, storageType, true);
    }

    public static StatisticKey<Float> hiddenFloatKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType, true);
    }

    public static StatisticKey<Double> hiddenDoubleKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType, true);
    }

    /**
     * {@link StatisticKey} ids cannot contain '.' characters as they could potentially cause issues with the backend.
     *
     * @param id The {@link Identifier} to check
     */
    private static void validateKeyId(Identifier id) {
        if (id.getNamespace().contains(".") || id.getPath().contains(".")) {
            throw new IllegalArgumentException("StatisticKey ids cannot contain '.'");
        }
    }

    public enum ValueType implements StringIdentifiable {
        INT("int"),
        FLOAT("float"),
        ;
        private final String name;

        ValueType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public enum StorageType implements StringIdentifiable {
        TOTAL("total"),
        ROLLING_AVERAGE("rolling_average"),
        MIN("min"),
        MAX("max"),
        ;
        private final String name;

        StorageType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
