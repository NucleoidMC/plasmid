package xyz.nucleoid.plasmid.party;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Collection;
import java.util.Collections;

// TODO: split party handling into its own mod
public final class PartyManager {
    private static PartyManager instance;

    private final MinecraftServer server;
    private final Object2ObjectMap<PlayerRef, Party> playerToParty = new Object2ObjectOpenHashMap<>();

    private PartyManager(MinecraftServer server) {
        this.server = server;
    }

    public static void initialize() {
        GameEvents.COLLECT_PLAYERS_FOR_JOIN.register((gameSpace, player, additional) -> {
            var partyManager = PartyManager.get(player.server);

            var members = partyManager.getPartyMembers(player);
            additional.addAll(members);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            PartyCommand.register(dispatcher);
        });
    }

    public static PartyManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new PartyManager(server);
        }
        return instance;
    }

    public void onPlayerLogOut(ServerPlayerEntity player) {
        var ref = PlayerRef.of(player);

        var party = this.playerToParty.remove(ref);
        if (party == null) {
            return;
        }

        if (party.remove(ref)) {
            this.onPartyOwnerLogOut(player, party);

            party.getMemberPlayers().sendMessage(PartyTexts.leftGame(player));
        }
    }

    private void onPartyOwnerLogOut(ServerPlayerEntity player, Party party) {
        var members = party.getMembers();

        if (!members.isEmpty()) {
            var nextMember = members.get(0);
            party.setOwner(nextMember);

            nextMember.ifOnline(this.server, nextPlayer -> {
                nextPlayer.sendMessage(PartyTexts.transferredReceiver(player), false);
            });
        }
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
            return this.disbandParty(party);
        } else {
            return PartyResult.err(PartyError.DOES_NOT_EXIST);
        }
    }

    public PartyResult disbandParty(Party party) {
        for (PlayerRef member : party.getMembers()) {
            this.playerToParty.remove(member, party);
        }
        return PartyResult.ok(party);
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
