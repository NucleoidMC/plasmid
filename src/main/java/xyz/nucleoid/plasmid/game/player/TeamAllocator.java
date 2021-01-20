package xyz.nucleoid.plasmid.game.player;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public final class TeamAllocator<T, V> {
    private final List<T> teams;
    private final List<V> players = new ArrayList<>();
    private final Map<V, T> teamPreferences = new Object2ObjectOpenHashMap<>();
    private final Object2IntMap<T> teamSizes = new Object2IntOpenHashMap<>();

    public TeamAllocator(Collection<T> teams) {
        Preconditions.checkArgument(!teams.isEmpty(), "cannot allocate with no teams");

        this.teams = new ArrayList<>(teams);
        this.teamSizes.defaultReturnValue(-1);
    }

    public void setSizeForTeam(T team, int maxSize) {
        Preconditions.checkArgument(this.teams.contains(team), "invalid team: " + team);
        Preconditions.checkArgument(maxSize > 0, "max team size must be >0");

        this.teamSizes.put(team, maxSize);
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

        // 1. place players evenly and randomly into all the teams
        this.placePlayersRandomly(teamToPlayers, playerToTeam);

        // 2. go through and try to swap players whose preferences mismatch with their assigned team
        this.optimizeTeamsByPreference(teamToPlayers, playerToTeam);

        return teamToPlayers;
    }

    private void placePlayersRandomly(Multimap<T, V> teamToPlayers, Map<V, T> playerToTeam) {
        List<T> availableTeams = new ArrayList<>(this.teams);
        List<V> players = new ArrayList<>(this.players);

        // shuffle the player and teams list for random initial allocation
        Collections.shuffle(availableTeams);
        Collections.shuffle(players);

        int teamIndex = 0;
        for (V player : players) {
            // all teams are full! we cannot allocate any more players
            if (availableTeams.isEmpty()) {
                throw new IllegalStateException("team overflow! all teams have exceeded maximum capacity");
            }

            T team = availableTeams.get(teamIndex);
            teamToPlayers.put(team, player);
            playerToTeam.put(player, team);

            // check for the maximum team size being exceeded
            int maxTeamSize = this.teamSizes.getInt(team);
            if (maxTeamSize != -1 && teamToPlayers.get(team).size() >= maxTeamSize) {
                // we've reached the maximum size for this team; exclude it
                availableTeams.remove(teamIndex);
            }

            teamIndex = (teamIndex + 1) % availableTeams.size();
        }
    }

    private void optimizeTeamsByPreference(Multimap<T, V> teamToPlayers, Map<V, T> playerToTeam) {
        List<V> players = new ArrayList<>(this.players);
        Collections.shuffle(players);

        for (V player : players) {
            T preference = this.teamPreferences.get(player);
            T current = playerToTeam.get(player);

            // we have no preference or we are already in our desired position, continue
            if (preference == null || current == preference) {
                continue;
            }

            Collection<V> currentTeamMembers = teamToPlayers.get(current);
            Collection<V> swapCandidates = teamToPlayers.get(preference);

            // we can move without swapping if the other team is smaller than ours if it has not exceeded the max size
            // we only care about keeping the teams balanced, so this is safe
            int maxSwapTeamSize = this.teamSizes.getInt(preference);
            if (swapCandidates.size() < currentTeamMembers.size() && swapCandidates.size() < maxSwapTeamSize) {
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
    }
}
