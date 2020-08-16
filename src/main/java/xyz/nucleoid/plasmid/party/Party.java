package xyz.nucleoid.plasmid.party;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class Party {
    private PlayerRef owner;

    private final Set<PlayerRef> members = new HashSet<>();
    private final Set<PlayerRef> pendingMembers = new HashSet<>();

    Party(PlayerRef owner) {
        this.setOwner(owner);
    }

    void setOwner(PlayerRef owner) {
        this.owner = owner;
        this.members.add(owner);
    }

    boolean invite(PlayerRef player) {
        if (this.members.contains(player)) {
            return false;
        }
        return this.pendingMembers.add(player);
    }

    boolean remove(PlayerRef player) {
        return this.members.remove(player) || this.pendingMembers.remove(player);
    }

    boolean acceptInvite(PlayerRef player) {
        if (this.pendingMembers.remove(player)) {
            return this.members.add(player);
        }
        return false;
    }

    public boolean contains(PlayerRef player) {
        return this.members.contains(player);
    }

    public boolean isOwner(PlayerRef from) {
        return from.equals(this.owner);
    }

    public Collection<PlayerRef> getMembers() {
        return this.members;
    }

    public void broadcastMessage(MinecraftServer server, Text message) {
        this.forOnline(server, player -> player.sendMessage(message, false));
    }

    public void forOnline(MinecraftServer server, Consumer<ServerPlayerEntity> consumer) {
        for (PlayerRef member : this.members) {
            member.ifOnline(server, consumer);
        }
    }
}
