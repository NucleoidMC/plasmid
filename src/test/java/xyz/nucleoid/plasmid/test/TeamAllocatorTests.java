package xyz.nucleoid.plasmid.test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import xyz.nucleoid.plasmid.api.game.common.team.TeamAllocator;

import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class TeamAllocatorTests {
    private static final int REPEAT_COUNT = 30;

    @RepeatedTest(REPEAT_COUNT)
    public void singleAllocationWithNoPreference() {
        var team = new Team("team");
        var player = new Player("player");

        assertAllocation(expected -> {
            expected.put(team, player);
        }, allocator -> {
            allocator.add(player, null);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void singleAllocationWithPreference() {
        var team = new Team("team");
        var player = new Player("player");

        assertAllocation(expected -> {
            expected.put(team, player);
        }, allocator -> {
            allocator.add(player, team);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void twoAllocationWithPartialPreference() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var player1 = new Player("player1");
        var player2 = new Player("player2");

        assertAllocation(expected -> {
            expected.put(team1, player1);
            expected.put(team2, player2);
        }, allocator -> {
            allocator.add(player1, team1);
            allocator.add(player2, null);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void twoAllocationWithPreference() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var player1 = new Player("player1");
        var player2 = new Player("player2");

        assertAllocation(expected -> {
            expected.put(team1, player2);
            expected.put(team2, player1);
        }, allocator -> {
            allocator.add(player1, team2);
            allocator.add(player2, team1);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void fourAllocationWithPartialPreference() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var player1 = new Player("player1");
        var player2 = new Player("player2");
        var player3 = new Player("player3");
        var player4 = new Player("player4");

        assertAllocation(expected -> {
            expected.put(team1, player1);
            expected.put(team1, player2);
            expected.put(team2, player3);
            expected.put(team2, player4);
        }, allocator -> {
            allocator.add(player1, team1);
            allocator.add(player2, team1);
            allocator.add(player3, team2);
            allocator.add(player4, null);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void fourAllocationWithPreference() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var player1 = new Player("player1");
        var player2 = new Player("player2");
        var player3 = new Player("player3");
        var player4 = new Player("player4");

        assertAllocation(expected -> {
            expected.put(team1, player2);
            expected.put(team1, player4);
            expected.put(team2, player1);
            expected.put(team2, player3);
        }, allocator -> {
            allocator.add(player1, team2);
            allocator.add(player2, team1);
            allocator.add(player3, team2);
            allocator.add(player4, team1);
        });
    }

    @RepeatedTest(REPEAT_COUNT)
    public void groupingAllocation() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var player1 = new Player("player1");
        var player2 = new Player("player2");
        var player3 = new Player("player3");
        var player4 = new Player("player4");

        assertAllocation(allocator -> {
            allocator.add(player1, null);
            allocator.add(player2, null);
            allocator.add(player3, null);
            allocator.add(player4, null);

            allocator.group(Set.of(player1, player3));
            allocator.group(Set.of(player2, player4));
        }, Set.of(team1, team2), actual -> {
            var team1Players = actual.get(team1);
            var team2Players = actual.get(team2);

            if (team1Players.contains(player1)) {
                assertTrue(team1Players.contains(player3), "expected player3 to be in team1");

                assertTrue(team2Players.contains(player2), "expected player2 to be in team2");
                assertTrue(team2Players.contains(player4), "expected player4 to be in team2");
            } else if (team2Players.contains(player1)) {
                assertTrue(team2Players.contains(player3), "expected player3 to be in team2");

                assertTrue(team1Players.contains(player2), "expected player2 to be in team1");
                assertTrue(team1Players.contains(player4), "expected player4 to be in team1");
            } else {
                fail("expected player1 to be in team1 or team2");
            }
        });
    }

    @Test
    public void uniqueGroupsFromMultipleRuns() {
        var team1 = new Team("team1");
        var team2 = new Team("team2");

        var allocator = createAllocator(Set.of(team1, team2));

        var players = IntStream.range(0, 20)
                .mapToObj(index -> new Player("player" + index))
                .collect(Collectors.toList());

        for (var player : players) {
            allocator.add(player, null);
        }

        allocator.group(players.subList(0, players.size() / 2));
        allocator.group(players.subList(players.size() / 2, players.size()));

        var allocations = new HashSet<HashMap<Team, Set<Player>>>();

        for (int i = 0; i < REPEAT_COUNT; i++) {
            var allocation = allocator.allocate();
            var orderedAllocation = new HashMap<Team, Set<Player>>();

            for (var entry : allocation.entries()) {
                var team = entry.getKey();
                var player = entry.getValue();

                var teamPlayers = orderedAllocation.computeIfAbsent(team, k -> new HashSet<>());
                teamPlayers.add(player);
            }

            allocations.add(orderedAllocation);
        }

        assertTrue(allocations.size() > 1, "expected multiple unique allocations, but found " + allocations);
    }

    @Test
    public void cannotAllocateWithNoTeams() {
        assertThrows(IllegalArgumentException.class, () -> {
            createAllocator(Set.of());
        });
    }

    @Test
    public void cannotGroupUnaddedPlayer() {
        var team = new Team("team");
        var player = new Player("player");

        var allocator = createAllocator(Set.of(team));

        assertThrows(IllegalArgumentException.class, () -> {
            allocator.group(Set.of(player));
        });
    }

    @Test
    public void cannotRegroupPlayer() {
        var team = new Team("team");
        var player = new Player("player");

        var allocator = createAllocator(Set.of(team));

        assertThrows(IllegalStateException.class, () -> {
            allocator.add(player, null);

            allocator.group(Set.of(player));
            allocator.group(Set.of(player));
        });
    }

    private static void assertAllocation(Consumer<TeamAllocator<Team, Player>> allocatorInitializer, Set<Team> teams, Consumer<Multimap<Team, Player>> validator) {
        var allocator = createAllocator(teams);
        allocatorInitializer.accept(allocator);

        var actual = allocator.allocate();
        validator.accept(actual);
    }

    private static void assertAllocation(Consumer<ImmutableMultimap.Builder<Team, Player>> expectedInitializer, Consumer<TeamAllocator<Team, Player>> allocatorInitializer) {
        var expectedBuilder = ImmutableMultimap.<Team, Player>builder();
        expectedInitializer.accept(expectedBuilder);

        var expected = expectedBuilder.build();

        var teams = expected.keySet();

        assertAllocation(allocatorInitializer, teams, actual -> {
            assertMultimapsEqual(expected, actual);
        });
    }

    private static void assertMultimapsEqual(Multimap<Team, Player> expected, Multimap<Team, Player> actual) {
        assertTrue(expected.keySet().equals(actual.keySet()), "expected teams " + expected.keySet() + " but got " + actual.keySet());

        for (Team key : expected.keySet()) {
            var expectedValue = expected.get(key);
            var actualValue = actual.get(key);

            Supplier<String> messageSupplier = () -> {
                return "expected team " + key.id() + " to contain " + expectedValue + ", but got " + actualValue;
            };

            assertTrue(expectedValue.containsAll(actualValue), messageSupplier);
            assertTrue(actualValue.containsAll(expectedValue), messageSupplier);
        }
    }

    private static TeamAllocator<Team, Player> createAllocator(Collection<Team> teams) {
        return new TeamAllocator<>(teams);
    }

    record Team(String id) {}
    record Player(String id) {}
}
