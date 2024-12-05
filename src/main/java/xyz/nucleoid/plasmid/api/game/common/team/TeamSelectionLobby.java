package xyz.nucleoid.plasmid.api.game.common.team;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.event.GameEvents;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents;
import xyz.nucleoid.plasmid.api.game.event.TeamSelectionLobbyEvents;
import xyz.nucleoid.plasmid.api.game.player.PlayerIterable;
import xyz.nucleoid.plasmid.impl.game.common.ui.element.TeamSelectionWaitingLobbyUiElement;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A very simple team selection lobby implementation that allows players to select a team while waiting to start a game.
 * <p>
 * This makes use of {@link TeamAllocator} in order to assign players teams fairly and take into account maximum team
 * sizes as well as team preferences.
 *
 * @see TeamSelectionLobby#allocate(PlayerIterable, BiConsumer)
 * @see GameTeamKey
 * @see TeamAllocator
 * @see GameWaitingLobby
 */
public final class TeamSelectionLobby {
    private final GameSpace gameSpace;
    private final GameTeamList teams;

    private final Reference2IntMap<GameTeamKey> maxTeamSize = new Reference2IntOpenHashMap<>();
    private final Map<UUID, GameTeamKey> teamPreference = new Object2ObjectOpenHashMap<>();

    private TeamSelectionLobby(GameSpace gameSpace, GameTeamList teams) {
        this.gameSpace = gameSpace;
        this.teams = teams;
    }

    /**
     * Applies this team selection lobby implementation to the given {@link GameActivity}.
     *
     * @param activity the activity to apply this lobby to
     * @param teams the teams to be available for selection
     * @return a new {@link TeamSelectionLobby} which can have teams added to it
     * @see TeamSelectionLobby#allocate(PlayerIterable, BiConsumer)
     */
    public static TeamSelectionLobby addTo(GameActivity activity, GameTeamList teams) {
        var lobby = new TeamSelectionLobby(activity.getGameSpace(), teams);
        activity.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT, lobby::onBuildUiLayout);

        return lobby;
    }

    /**
     * Sets the maximum number of players that can be allocated to the given team.
     *
     * @param team the team to set a maximum size for
     * @param size the maximum number of players that can be allocated
     */
    public void setSizeForTeam(GameTeamKey team, int size) {
        this.maxTeamSize.put(team, size);
    }

    private void onBuildUiLayout(WaitingLobbyUiLayout layout, ServerPlayerEntity player) {
        // Spectators cannot choose a team
        if (!this.gameSpace.getPlayers().participants().contains(player)) {
            return;
        }

        layout.addLeading(new TeamSelectionWaitingLobbyUiElement(teams, key -> {
            return key == this.teamPreference.get(player.getUuid());
        }, key -> {
            var team = this.teams.byKey(key);
            if (team != null) {
                var config = team.config();

                this.teamPreference.put(player.getUuid(), key);
                layout.refresh();

                var message = Text.translatable("text.plasmid.team_selection.requested_team",
                        Text.translatable("text.plasmid.team_selection.suffixed_team", config.name()).formatted(config.chatFormatting()));

                player.sendMessage(message, false);
            }
        }));
    }

    /**
     * Allocates all the players within this lobby into teams depending on their specified preferences.
     *
     * @param players all players to be allocated
     * @param apply a consumer that accepts each player and their corresponding team
     * @see TeamAllocator
     */
    public void allocate(PlayerIterable players, BiConsumer<GameTeamKey, ServerPlayerEntity> apply) {
        var teamKeys = this.teams.stream().map(GameTeam::key).collect(Collectors.toList());
        var allocator = new TeamAllocator<GameTeamKey, ServerPlayerEntity>(teamKeys);

        for (var entry : Reference2IntMaps.fastIterable(this.maxTeamSize)) {
            allocator.setSizeForTeam(entry.getKey(), entry.getIntValue());
        }

        for (var player : players) {
            GameTeamKey preference = this.teamPreference.get(player.getUuid());
            allocator.add(player, preference);
        }

        this.gameSpace.getBehavior().invoker(TeamSelectionLobbyEvents.FINALIZE).onFinalizeTeamSelection(allocator, players);
        GameEvents.TEAM_SELECTION_LOBBY_FINALIZE.invoker().onFinalizeTeamSelection(gameSpace, allocator, players);

        allocator.allocate(apply);
    }
}
