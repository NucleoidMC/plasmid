package xyz.nucleoid.plasmid.game.channel;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public final class ChannelConnections {
    private final Set<ChannelEndpoint> connections = new ObjectOpenHashSet<>();

    public void updateDisplay(GameChannel channel) {
        if (this.connections.isEmpty()) {
            return;
        }

        GameChannelDisplay display = channel.display();
        for (ChannelEndpoint connection : this.connections) {
            connection.updateDisplay(display);
        }
    }

    public boolean connectTo(GameChannel channel, ChannelEndpoint endpoint) {
        if (endpoint.getConnection() == null && this.connections.add(endpoint)) {
            endpoint.setConnection(channel);
            endpoint.updateDisplay(channel.display());
            return true;
        }
        return false;
    }

    public boolean removeConnection(ChannelEndpoint endpoint) {
        if (this.connections.remove(endpoint)) {
            endpoint.invalidateConnection();
            return true;
        }
        return false;
    }

    public void invalidate() {
        for (ChannelEndpoint connection : this.connections) {
            connection.invalidateConnection();
        }
        this.connections.clear();
    }
}
