package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.Plasmid;

import java.util.ArrayList;
import java.util.List;

public final class GameResources implements AutoCloseable {
    private final List<AutoCloseable> resources = new ArrayList<>();

    public synchronized <T extends AutoCloseable> T add(T resource) {
        this.resources.add(resource);
        return resource;
    }

    @Override
    public synchronized void close() {
        for (AutoCloseable resource : this.resources) {
            try {
                resource.close();
            } catch (Exception e) {
                Plasmid.LOGGER.warn("Failed to close resource for game", e);
            }
        }
        this.resources.clear();
    }
}
