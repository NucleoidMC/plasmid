package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.BiConsumer;

/**
 * Stores a mapping of {@link StatisticKey} to their corresponding values
 */
public class StatisticMap {
    protected static final StatisticMap EMPTY = new StatisticMap() {
        @Override
        public void increment(StatisticKey<Double> key, double amount) {
            return;
        }

        @Override
        public void increment(StatisticKey<Float> key, float amount) {
            return;
        }

        @Override
        public void increment(StatisticKey<Integer> key, int amount) {
            return;
        }

        @Override
        public <T extends Number> T get(StatisticKey<T> key, T defaultValue) {
            return defaultValue;
        }

        @Override
        public void set(StatisticKey<Double> key, double value) {
            return;
        }

        @Override
        public void set(StatisticKey<Float> key, float value) {
            return;
        }

        @Override
        public void set(StatisticKey<Integer> key, int value) {
            return;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private final Object2ObjectMap<StatisticKey<?>, Number> values = new Object2ObjectOpenHashMap<>();

    public void increment(StatisticKey<Double> key, double amount) {
        this.values.put(key, (Double) this.values.getOrDefault(key, 0.0d) + amount);
    }

    public void increment(StatisticKey<Float> key, float amount) {
        this.values.put(key, (Float) this.values.getOrDefault(key, 0.0f) + amount);
    }

    public void increment(StatisticKey<Integer> key, int amount) {
        this.values.put(key, (Integer) this.values.getOrDefault(key, 0) + amount);
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T get(StatisticKey<T> key, T defaultValue) {
        return (T) this.values.getOrDefault(key, defaultValue);
    }

    public void set(StatisticKey<Double> key, double value) {
        this.values.put(key, value);
    }

    public void set(StatisticKey<Float> key, float value) {
        this.values.put(key, value);
    }

    public void set(StatisticKey<Integer> key, int value) {
        this.values.put(key, value);
    }

    /**
     * Write this bundle to a new {@link JsonObject} ready to be sent to a backend
     *
     * @return A {@link JsonObject} containing all of the encoded values
     */
    public JsonObject encode() {
        JsonObject obj = new JsonObject();

        for (Object2ObjectMap.Entry<StatisticKey<?>, Number> entry : this.values.object2ObjectEntrySet()) {
            JsonObject stat = entry.getKey().encodeValueUnchecked(entry.getValue());
            obj.add(entry.getKey().id().toString(), stat);
        }

        return obj;
    }

    public void visitAllStatistics(BiConsumer<StatisticKey<?>, Number> visitor) {
        for (var entry : this.values.object2ObjectEntrySet()) {
            visitor.accept(entry.getKey(), entry.getValue());
        }
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }
}
