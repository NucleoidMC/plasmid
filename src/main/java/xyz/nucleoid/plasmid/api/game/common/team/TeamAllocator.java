package xyz.nucleoid.plasmid.api.game.common.team;

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
        this.teamSizes.defaultReturnValue(Integer.MAX_VALUE);
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
        var allocations = new Allocations<T, V>();

        // 1. place players evenly and randomly into all the teams
        this.placePlayersRandomly(allocations);

        // 2. go through and try to swap players whose preferences mismatch with their assigned team
        this.optimizeTeamsByPreference(allocations);

        return allocations.teamToPlayers;
    }

    private void placePlayersRandomly(Allocations<T, V> allocations) {
        var availableTeams = new ArrayList<>(this.teams);
        var players = new ArrayList<>(this.players);

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
            allocations.setTeam(player, team);

            // check for the maximum team size being exceeded
            int maxTeamSize = this.teamSizes.getInt(team);
            if (allocations.playersIn(team).size() >= maxTeamSize) {
                // we've reached the maximum size for this team; exclude it from further consideration
                availableTeams.remove(teamIndex);
            }

            teamIndex = (teamIndex + 1) % availableTeams.size();
        }
    }

    private void optimizeTeamsByPreference(Allocations<T, V> allocations) {
        var players = new ArrayList<V>(this.players);
        Collections.shuffle(players);

        for (var player : players) {
            var preference = this.teamPreferences.get(player);
            var current = allocations.teamFor(player);

            // we have no preference or we are already in our desired position, continue
            if (preference == null || current == preference) {
                continue;
            }

            var currentPlayers = allocations.playersIn(current);
            var preferencePlayers = allocations.playersIn(preference);

            // we can move without swapping if the other team is smaller than ours if it has not exceeded the max size
            // we only care about keeping the teams balanced, so this is safe
            if (preferencePlayers.size() < currentPlayers.size() && this.canTeamGrow(preference, preferencePlayers.size())) {
                allocations.moveTeam(player, current, preference);
            } else {
                this.trySwapWithOtherPlayer(allocations, player, current, preference);
            }
        }
    }

    private boolean canTeamGrow(T team, int size) {
        int maxSize = this.teamSizes.getInt(team);
        return size < maxSize;
    }

    private void trySwapWithOtherPlayer(Allocations<T, V> allocations, V player, T from, T to) {
        var swapWith = this.findSwapCandidate(from, to, allocations.playersIn(to));
        if (swapWith != null) {
            allocations.moveTeam(player, from, to);
            allocations.moveTeam(swapWith, to, from);
        }
    }

    @Nullable
    private V findSwapCandidate(T from, T to, Collection<V> candidates) {
        V swapWith = null;

        for (var candidate : candidates) {
            var candidatePreference = this.teamPreferences.get(candidate);
            if (candidatePreference == to) {
                // we can't swap with this player: they are already in their chosen team
                continue;
            }

            // prioritise players who want to join our current team
            if (swapWith == null || candidatePreference == from) {
                swapWith = candidate;
            }
        }

        return swapWith;
    }

    static final class Allocations<T, V> {
        final Multimap<T, V> teamToPlayers = HashMultimap.create();
        final Map<V, T> playerToTeam = new Object2ObjectOpenHashMap<>();

        void setTeam(V player, T team) {
            this.teamToPlayers.put(team, player);
            this.playerToTeam.put(player, team);
        }

        T teamFor(V player) {
            return this.playerToTeam.get(player);
        }

        Collection<V> playersIn(T team) {
            return this.teamToPlayers.get(team);
        }

        void moveTeam(V player, T from, T to) {
            this.teamToPlayers.remove(from, player);
            this.teamToPlayers.put(to, player);
            this.playerToTeam.put(player, to);
        }
    }
}
