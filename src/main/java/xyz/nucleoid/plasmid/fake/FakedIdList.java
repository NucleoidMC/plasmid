package xyz.nucleoid.plasmid.fake;

import net.minecraft.util.collection.IdList;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class FakedIdList<T> extends IdList<T> {
    private final IdList<T> parent;

    public FakedIdList(IdList<T> parent) {
        super(0);
        this.parent = parent;
    }

    @Override
    public void set(T value, int id) {
        this.parent.set(value, id);
    }

    @Override
    public void add(T value) {
        this.parent.add(value);
    }

    @Override
    @Nullable
    public T get(int index) {
        return this.parent.get(index);
    }

    @Override
    public int getRawId(T object) {
        return this.parent.getRawId(Fake.getProxy(object));
    }

    @Override
    public Iterator<T> iterator() {
        return this.parent.iterator();
    }

    @Override
    public int size() {
        return this.parent.size();
    }
}
