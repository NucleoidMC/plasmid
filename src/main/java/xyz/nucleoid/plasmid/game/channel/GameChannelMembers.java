package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;

public final class GameChannelMembers {
    private final GameChannel channel;
    private final MutablePlayerSet players;

    public GameChannelMembers(MinecraftServer server, GameChannel channel) {
        this.channel = channel;
        this.players = new MutablePlayerSet(server);
    }

    public void addPlayer(ServerPlayerEntity player) {
        if (this.players.add(player)) {
            this.channel.updateDisplay();
        }
    }

    public void removePlayer(ServerPlayerEntity player) {
        if (this.players.remove(player)) {
            this.channel.updateDisplay();
        }
    }

    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.players.contains(player);
    }

    public void clear() {
        this.players.clear();
        this.channel.updateDisplay();
    }

    public int size() {
        return this.players.size();
    }
}
