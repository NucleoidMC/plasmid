package xyz.nucleoid.plasmid.game.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public final class TeamAllocator<T, V> {
    private final List<T> teams;
    private final List<V> players = new ArrayList<>();
    private final Map<V, T> teamPreferences = new Object2ObjectOpenHashMap<>();

    public TeamAllocator(Collection<T> teams) {
        this.teams = new ArrayList<>(teams);
    }

    public void add(V player, @Nullable T preference) {
        this.players.add(player);
        if (preference != null) {
            this.teamPreferences.put(player, preference);
        }
    }

    public void allocate(BiConsumer<T, V> apply) {
        Multimap<T, V> teamToPlayers = this.build();
        teamToPlayers.forEach(apply);
    }

    public Multimap<T, V> build() {
        Multimap<T, V> teamToPlayers = HashMultimap.create();
        Map<V, T> playerToTeam = new Object2ObjectOpenHashMap<>();

        // shuffle the player and teams list for random initial allocation
        Collections.shuffle(this.teams);
        Collections.shuffle(this.players);

        // 1. place everyone in all the teams in an even distribution
        int teamIndex = 0;
        for (V player : this.players) {
            T team = this.teams.get(teamIndex++ % this.teams.size());
            teamToPlayers.put(team, player);
            playerToTeam.put(player, team);
        }

        // we want to do swapping in a different order to how we initially allocated
        Collections.shuffle(this.players);

        // 2. go through and try to swap players whose preferences mismatch with their assigned team
        for (V player : this.players) {
            T preference = this.teamPreferences.get(player);
            T current = playerToTeam.get(player);

            // we have no preference or we are already in our desired position, continue
            if (preference == null || current == preference) {
                continue;
            }

            Collection<V> currentTeamMembers = teamToPlayers.get(current);
            Collection<V> swapCandidates = teamToPlayers.get(preference);

            // we can move without swapping if the other team is smaller than ours
            // we only care about keeping the teams balanced, so this is safe
            if (swapCandidates.size() < currentTeamMembers.size()) {
                teamToPlayers.remove(current, player);
                teamToPlayers.put(preference, player);
                playerToTeam.put(player, preference);
            } else {
                V swapWith = null;

                for (V swapCandidate : swapCandidates) {
                    T swapCandidatePreference = this.teamPreferences.get(swapCandidate);
                    if (swapCandidatePreference == preference) {
                        // we can't swap with this player: they are already in their chosen team
                        continue;
                    }

                    // we want to prioritise swapping with someone who wants to join our team
                    if (swapWith == null || swapCandidatePreference == current) {
                        swapWith = swapCandidate;
                    }
                }

                // we found somebody to swap with! swap 'em
                if (swapWith != null) {
                    teamToPlayers.remove(current, player);
                    teamToPlayers.put(preference, player);
                    playerToTeam.put(player, preference);

                    teamToPlayers.remove(preference, swapWith);
                    teamToPlayers.put(current, swapWith);
                    playerToTeam.put(swapWith, current);
                }
            }
        }

        return teamToPlayers;
    }
}
