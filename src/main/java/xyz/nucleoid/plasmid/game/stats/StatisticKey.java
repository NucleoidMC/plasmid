package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * Type-safe keys for identifying statistics.
 * <p>
 * Ideally should have a corresponding translation key in the form <code>statistic.[bundle namespace].[key namespace].[key path]</code>
 * or <code>statistic.[key namespace].[key path]</code> for future-proofing
 * <p>
 * See {@link StatisticKeys} for some general keys for minigames to make use of.
 *
 * @param <T> The type of {@link Number} this key stores
 * @see StatisticKeys
 */
public class StatisticKey<T extends Number> {
    protected final Identifier id;
    private final ValueType valueType;
    protected final StorageType storageType;

    private StatisticKey(Identifier id, ValueType valueType, StorageType storageType) {
        this.valueType = valueType;
        StatisticKey.validateKeyId(id);
        this.id = id;
        this.storageType = storageType;
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

    /**
     * Returns the translation key used to name this {@link StatisticKey} in a generic context.
     * <p>
     * Note: When referring to a {@link StatisticKey} in a specific context (eg. inside a {@link GameStatisticBundle})
     * then you should also check for the existence of the translation key from {@link StatisticKey#getTranslationKey(String)}
     *
     * @return A translation key in the form <code>statistic.[key namespace].[key path]</code>
     * @see StatisticKey#getTranslationKey(String)
     */
    public String getTranslationKey() {
        return "statistic." + this.id.getNamespace() + "." + this.id.getPath();
    }

    /**
     * Returns the translation key used to name this {@link StatisticKey} in a more specific context (eg inside a {@link GameStatisticBundle}.
     * <p>
     * Note: The caller should check if the returned translation key exists and if it does not it should fall back to the key from {@link StatisticKey#getTranslationKey()}
     *
     * @param bundle The namespace of the bundle this key is in.
     * @return A translation key in the form <code>statistic.[bundle namespace].[key namespace].[key path]</code>
     * @see StatisticKey#getTranslationKey()
     */
    public String getTranslationKey(String bundle) {
        return "statistic." + bundle + "." + this.id.getNamespace() + "." + this.id.getPath();
    }

    private String encodeType() {
        return this.valueType.asString() + "_" + this.storageType.asString();
    }

    public static StatisticKey<Integer> intKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.INT, storageType);
    }

    public static StatisticKey<Float> floatKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType);
    }

    public static StatisticKey<Double> doubleKey(Identifier id, StorageType storageType) {
        return new StatisticKey<>(id, ValueType.FLOAT, storageType);
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
