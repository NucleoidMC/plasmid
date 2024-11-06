package xyz.nucleoid.plasmid.impl.portal;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class GamePortalDisplay {
    public static final Field<Text> NAME = Field.create();
    public static final Field<Integer> PLAYER_COUNT = Field.create();
    public static final Field<Integer> ACTIVE_GAMES = Field.create();

    private final Reference2ObjectMap<Field<?>, Object> fields = new Reference2ObjectOpenHashMap<>();

    public void clear() {
        this.fields.clear();
    }

    public <T> void set(Field<T> field, T value) {
        this.fields.put(field, value);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(Field<T> field) {
        return (T) this.fields.get(field);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;

        var display = (GamePortalDisplay) obj;
        return this.fields.equals(display.fields);
    }

    @Override
    public int hashCode() {
        return this.fields.hashCode();
    }

    public static final class Field<T> {
        private Field() {
        }

        public static <T> Field<T> create() {
            return new Field<>();
        }
    }
}
