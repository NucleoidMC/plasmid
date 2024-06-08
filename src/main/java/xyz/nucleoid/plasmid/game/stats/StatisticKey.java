package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
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
        obj.addProperty("type", this.valueType.asString());
        obj.addProperty("value", value);
        obj.addProperty("hidden", this.hidden);
        return obj;
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("statistic", this.id);
    }

    public static StatisticKey<Integer> intKey(Identifier id) {
        return new StatisticKey<>(id, ValueType.INT, false);
    }

    public static StatisticKey<Float> floatKey(Identifier id) {
        return new StatisticKey<>(id, ValueType.FLOAT, false);
    }

    public static StatisticKey<Double> doubleKey(Identifier id) {
        return new StatisticKey<>(id, ValueType.FLOAT, false);
    }

    public StatisticKey<T> hidden(boolean hidden) {
        return new StatisticKey<>(this.id, this.valueType, hidden);
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
        INT("int_total"),
        FLOAT("float_total"),
        ;

        public static final Codec<ValueType> CODEC = StringIdentifiable.createCodec(ValueType::values);

        private final String name;

        ValueType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
