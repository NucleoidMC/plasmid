package xyz.nucleoid.plasmid.game.common.team;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Utility class for allocating players into teams that takes into account maximum team sizes as well as team
 * preferences.
 * <p>
 * Given a team preference, the allocator will try its best to satisfy as many players as possible while remaining fair
 * and balanced.
 *
 * @param <T> the team type
 * @param <V> the "player" type
 */
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

    /**
     * Sets the maximum amount of players that can be allocated to the given team.
     *
     * @param team the team to set a maximum size for
     * @param maxSize the maximum number of players that can be allocated to this team
     */
    public void setSizeForTeam(T team, int maxSize) {
        Preconditions.checkArgument(this.teams.contains(team), "invalid team: " + team);
        Preconditions.checkArgument(maxSize > 0, "max team size must be >0");

        this.teamSizes.put(team, maxSize);
    }

    /**
     * Adds a player for consideration by this {@link TeamAllocator} with an optional team preference.
     *
     * @param player the player to add to this allocator
     * @param preference the preference team for this player, or {@code null} if no preference
     */
    public void add(V player, @Nullable T preference) {
        this.players.add(player);
        if (preference != null) {
            this.teamPreferences.put(player, preference);
        }
    }

    /**
     * Allocates all players added through {@link TeamAllocator#add} into teams, taking preference and max size into
     * account.
     *
     * @param apply a consumer that is called for each player and their allocated team
     */
    public void allocate(BiConsumer<T, V> apply) {
        var teamToPlayers = this.allocate();
        teamToPlayers.forEach(apply);
    }

    /**
     * Allocates all players added through {@link TeamAllocator#add} into teams, taking preference and max size into
     * account.
     *
     * @return a {@link Multimap} containing every team and the allocated players
     */
    public Multimap<T, V> allocate() {
        var teamToPlayers = HashMultimap.<T, V>create();
        var playerToTeam = new Object2ObjectOpenHashMap<V, T>();

        // 1. place players evenly and randomly into all the teams
        this.placePlayersRandomly(teamToPlayers, playerToTeam);

        // 2. go through and try to swap players whose preferences mismatch with their assigned team
        this.optimizeTeamsByPreference(teamToPlayers, playerToTeam);

        return teamToPlayers;
    }

    private void placePlayersRandomly(Multimap<T, V> teamToPlayers, Map<V, T> playerToTeam) {
        var availableTeams = new ArrayList<T>(this.teams);
        var players = new ArrayList<V>(this.players);

        // shuffle the player and teams list for random initial allocation
        Collections.shuffle(availableTeams);
        Collections.shuffle(players);

        int teamIndex = 0;
        for (var player : players) {
            // all teams are full! we cannot allocate any more players
            if (availableTeams.isEmpty()) {
                throw new IllegalStateException("team overflow! all teams have exceeded maximum capacity");
            }

            var team = availableTeams.get(teamIndex);
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
        var players = new ArrayList<V>(this.players);
        Collections.shuffle(players);

        for (var player : players) {
            var preference = this.teamPreferences.get(player);
            var current = playerToTeam.get(player);

            // we have no preference or we are already in our desired position, continue
            if (preference == null || current == preference) {
                continue;
            }

            var currentTeamMembers = teamToPlayers.get(current);
            var swapCandidates = teamToPlayers.get(preference);

            // we can move without swapping if the other team is smaller than ours if it has not exceeded the max size
            // we only care about keeping the teams balanced, so this is safe
            int maxSwapTeamSize = this.teamSizes.getInt(preference);
            if (swapCandidates.size() < currentTeamMembers.size() && swapCandidates.size() < maxSwapTeamSize) {
                teamToPlayers.remove(current, player);
                teamToPlayers.put(preference, player);
                playerToTeam.put(player, preference);
            } else {
                V swapWith = null;

                for (var swapCandidate : swapCandidates) {
                    var swapCandidatePreference = this.teamPreferences.get(swapCandidate);
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
