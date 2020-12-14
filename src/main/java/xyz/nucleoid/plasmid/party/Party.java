package xyz.nucleoid.plasmid.party;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Collection;
import java.util.Set;

public final class Party {
    private PlayerRef owner;

    private final Set<PlayerRef> members = new ObjectOpenHashSet<>();
    private final Set<PlayerRef> pendingMembers = new ObjectOpenHashSet<>();

    private final MutablePlayerSet memberPlayers;

    Party(MinecraftServer server, PlayerRef owner) {
        this.memberPlayers = new MutablePlayerSet(server);
        this.setOwner(owner);
    }

    void setOwner(PlayerRef owner) {
        this.owner = owner;
        this.members.add(owner);
        this.memberPlayers.add(owner);
    }

    boolean invite(PlayerRef player) {
        if (this.members.contains(player)) {
            return false;
        }
        return this.pendingMembers.add(player);
    }

    boolean remove(PlayerRef player) {
        if (this.members.remove(player)) {
            this.memberPlayers.remove(player);
            return true;
        }
        return this.pendingMembers.remove(player);
    }

    boolean acceptInvite(PlayerRef player) {
        if (this.pendingMembers.remove(player)) {
            if (this.members.add(player)) {
                this.memberPlayers.add(player);
                return true;
            }
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

    public MutablePlayerSet getMemberPlayers() {
        return this.memberPlayers;
    }
}
