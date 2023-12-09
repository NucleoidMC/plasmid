package xyz.nucleoid.plasmid.util;

import it.unimi.dsi.fastutil.Hash;

public enum IdentityHashStrategy implements Hash.Strategy<Object> {
    INSTANCE;

    @Override
    public int hashCode(Object o) {
        return System.identityHashCode(o);
    }

    @Override
    public boolean equals(Object o, Object k1) {
        return false;
    }
}
