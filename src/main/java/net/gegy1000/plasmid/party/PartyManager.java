package net.gegy1000.plasmid.party;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.gegy1000.plasmid.util.PlayerRef;

import javax.annotation.Nullable;

public final class PartyManager {
    public static final PartyManager INSTANCE = new PartyManager();

    private final Object2ObjectMap<PlayerRef, Party> playerToParty = new Object2ObjectOpenHashMap<>();

    private PartyManager() {
    }

    public PartyResult invitePlayer(PlayerRef owner, PlayerRef player) {
        Party party = this.getOrCreateOwnParty(owner);
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

        Party party = this.getOwnParty(owner);
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

        Party party = this.getOwnParty(owner);
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
        Party party = this.getParty(player);
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
        Party party = this.getOwnParty(from);
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
        Party party = this.getOwnParty(owner);
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
        Party party = this.playerToParty.get(owner);
        if (party != null && party.isOwner(owner)) {
            return party;
        }
        return null;
    }

    private Party getOrCreateOwnParty(PlayerRef owner) {
        Party party = this.playerToParty.computeIfAbsent(owner, Party::new);
        if (party.isOwner(owner)) {
            return party;
        }
        return null;
    }
}
