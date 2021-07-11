package xyz.nucleoid.plasmid.party;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.List;
import java.util.Set;

public final class Party {
    private PlayerRef owner;

    private final List<PlayerRef> members = new ObjectArrayList<>();
    private final Set<PlayerRef> pendingMembers = new ObjectOpenHashSet<>();

    private final MutablePlayerSet memberPlayers;

    Party(MinecraftServer server, PlayerRef owner) {
        this.memberPlayers = new MutablePlayerSet(server);
        this.setOwner(owner);
    }

    void setOwner(PlayerRef owner) {
        this.owner = owner;
        if (this.memberPlayers.add(owner)) {
            this.members.add(owner);
        }
    }

    boolean invite(PlayerRef player) {
        if (this.memberPlayers.contains(player)) {
            return false;
        }
        return this.pendingMembers.add(player);
    }

    boolean remove(PlayerRef player) {
        if (this.memberPlayers.remove(player)) {
            this.members.remove(player);
            return true;
        }
        return this.pendingMembers.remove(player);
    }

    boolean acceptInvite(PlayerRef player) {
        if (this.pendingMembers.remove(player)) {
            if (this.memberPlayers.add(player)) {
                this.members.add(player);
            }
            return true;
        }
        return false;
    }

    public boolean contains(PlayerRef player) {
        return this.memberPlayers.contains(player);
    }

    public boolean isOwner(PlayerRef from) {
        return from.equals(this.owner);
    }

    public List<PlayerRef> getMembers() {
        return this.members;
    }

    public MutablePlayerSet getMemberPlayers() {
        return this.memberPlayers;
    }
}
