package xyz.nucleoid.plasmid.party;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Collection;
import java.util.Collections;

// TODO: does not handle players leaving the server!
public final class PartyManager {
    private static PartyManager instance;

    private final MinecraftServer server;
    private final Object2ObjectMap<PlayerRef, Party> playerToParty = new Object2ObjectOpenHashMap<>();

    private PartyManager(MinecraftServer server) {
        this.server = server;
    }

    public static PartyManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new PartyManager(server);
        }
        return instance;
    }

    public PartyResult invitePlayer(PlayerRef owner, PlayerRef player) {
        var party = this.getOrCreateOwnParty(owner);
        if (party != null) {
            if (party.invite(player)) {
                return PartyResult.ok(party);
            } else {
                return PartyResult.err(PartyError.ALREADY_INVITED);
            }
        }

        return PartyResult.err(PartyError.DOES_NOT_EXIST);
    }

    public PartyResult kickPlayer(PlayerRef owner, PlayerRef player) {
        if (owner.equals(player)) {
            return PartyResult.err(PartyError.CANNOT_REMOVE_SELF);
        }

        var party = this.getOwnParty(owner);
        if (party == null) {
            return PartyResult.err(PartyError.DOES_NOT_EXIST);
        }

        if (party.remove(player)) {
            this.playerToParty.remove(player, party);
            return PartyResult.ok(party);
        }

        return PartyResult.err(PartyError.NOT_IN_PARTY);
    }

    public PartyResult acceptInvite(PlayerRef player, PlayerRef owner) {
        if (this.playerToParty.containsKey(player)) {
            return PartyResult.err(PartyError.ALREADY_IN_PARTY);
        }

        var party = this.getOwnParty(owner);
        if (party == null) {
            return PartyResult.err(PartyError.DOES_NOT_EXIST);
        }

        if (party.acceptInvite(player)) {
            this.playerToParty.put(player, party);
            return PartyResult.ok(party);
        }

        return PartyResult.err(PartyError.NOT_INVITED);
    }

    public PartyResult leaveParty(PlayerRef player) {
        var party = this.getParty(player);
        if (party == null) {
            return PartyResult.err(PartyError.DOES_NOT_EXIST);
        }

        if (party.isOwner(player)) {
            if (party.getMembers().size() > 1) {
                return PartyResult.err(PartyError.CANNOT_REMOVE_SELF);
            }
            return this.disbandParty(player);
        }

        if (party.remove(player)) {
            this.playerToParty.remove(player, party);
            return PartyResult.ok(party);
        } else {
            return PartyResult.err(PartyError.NOT_IN_PARTY);
        }
    }

    public PartyResult transferParty(PlayerRef from, PlayerRef to) {
        var party = this.getOwnParty(from);
        if (party == null) {
            return PartyResult.err(PartyError.DOES_NOT_EXIST);
        }

        if (!party.contains(to)) {
            return PartyResult.err(PartyError.NOT_IN_PARTY);
        }

        party.setOwner(to);
        return PartyResult.ok(party);
    }

    public PartyResult disbandParty(PlayerRef owner) {
        var party = this.getOwnParty(owner);
        if (party != null) {
            for (PlayerRef member : party.getMembers()) {
                this.playerToParty.remove(member, party);
            }
            return PartyResult.ok(party);
        }

        return PartyResult.err(PartyError.DOES_NOT_EXIST);
    }

    @Nullable
    public Party getParty(PlayerRef player) {
        return this.playerToParty.get(player);
    }

    @Nullable
    public Party getOwnParty(PlayerRef owner) {
        var party = this.playerToParty.get(owner);
        if (party != null && party.isOwner(owner)) {
            return party;
        }
        return null;
    }

    private Party getOrCreateOwnParty(PlayerRef owner) {
        var party = this.playerToParty.computeIfAbsent(owner, o -> new Party(this.server, o));
        if (party.isOwner(owner)) {
            return party;
        }
        return null;
    }

    public Collection<ServerPlayerEntity> getPartyMembers(ServerPlayerEntity player) {
        var party = this.getOwnParty(PlayerRef.of(player));
        if (party != null) {
            return Lists.newArrayList(party.getMemberPlayers());
        } else {
            return Collections.singleton(player);
        }
    }
}
