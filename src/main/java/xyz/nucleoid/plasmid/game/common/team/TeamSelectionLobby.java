package xyz.nucleoid.plasmid.game.common.team;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerIterable;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * A very simple team selection lobby implementation that allows players to select a team while waiting to start a game.
 * <p>
 * This makes use of {@link TeamAllocator} in order to assign players teams fairly and take into account maximum team
 * sizes as well as team preferences.
 *
 * @see TeamSelectionLobby#addTeam(GameTeam, GameTeamConfig)
 * @see TeamSelectionLobby#allocate(PlayerIterable, BiConsumer)
 * @see GameTeam
 * @see TeamAllocator
 * @see xyz.nucleoid.plasmid.game.common.GameWaitingLobby
 */
public final class TeamSelectionLobby {
    private static final String TEAM_KEY = Plasmid.ID + ":team";

    private final Map<GameTeam, GameTeamConfig> teams = new Object2ObjectOpenHashMap<>();

    private final Reference2IntMap<GameTeam> maxTeamSize = new Reference2IntOpenHashMap<>();
    private final Map<UUID, GameTeam> teamPreference = new Object2ObjectOpenHashMap<>();

    private TeamSelectionLobby() {
    }

    /**
     * Applies this team selection lobby implementation to the given {@link GameActivity}.
     *
     * @param activity the activity to apply this lobby to
     * @return a new {@link TeamSelectionLobby} which can have teams added to it
     * @see TeamSelectionLobby#addTeam(GameTeam, GameTeamConfig)
     * @see TeamSelectionLobby#allocate(PlayerIterable, BiConsumer)
     */
    public static TeamSelectionLobby addTo(GameActivity activity) {
        var lobby = new TeamSelectionLobby();
        activity.listen(GamePlayerEvents.ADD, lobby::onAddPlayer);
        activity.listen(ItemUseEvent.EVENT, lobby::onUseItem);

        return lobby;
    }

    /**
     * Adds a team that can be selected by players within this {@link TeamSelectionLobby}.
     * This must be run before players join this lobby, or they will not receive the team items!
     *
     * @param team the team to add
     * @param config the configuration for the added team
     */
    public void addTeam(GameTeam team, GameTeamConfig config) {
        this.teams.put(team, config);
    }

    /**
     * Adds a collection of teams that can be selected by players within this {@link TeamSelectionLobby}.
     * This must be run before players join this lobby, or they will not receive the team items!
     *
     * @param teams the collection of teams to add
     */
    public void addTeams(GameTeamsConfig teams) {
        teams.map().forEach(this::addTeam);
    }

    /**
     * Sets the maximum number of players that can be allocated to the given team.
     *
     * @param team the team to set a maximum size for
     * @param size the maximum number of players that can be allocated
     */
    public void setSizeForTeam(GameTeam team, int size) {
        this.maxTeamSize.put(team, size);
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        int index = 0;

        for (var entry : this.teams.entrySet()) {
            var team = entry.getKey();
            var config = entry.getValue();

            var name = new TranslatableText("text.plasmid.team_selection.request_team", config.name())
                    .formatted(Formatting.BOLD, config.chatFormatting());

            var stack = new ItemStack(ColoredBlocks.wool(config.blockDyeColor()));
            stack.setCustomName(name);

            stack.getOrCreateTag().putString(TEAM_KEY, team.id());

            player.getInventory().setStack(index++, stack);
        }
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);

        if (stack.isIn(ItemTags.WOOL)) {
            var tag = stack.getOrCreateTag();
            var team = new GameTeam(tag.getString(TEAM_KEY));

            var config = this.teams.get(team);
            if (config != null) {
                this.teamPreference.put(player.getUuid(), team);

                var message = new TranslatableText("text.plasmid.team_selection.requested_team",
                        new TranslatableText("text.plasmid.team_selection.suffixed_team", config.name()).formatted(config.chatFormatting()));

                player.sendMessage(message, false);

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    /**
     * Allocates all the players within this lobby into teams depending on their specified preferences.
     *
     * @param players all players to be allocated
     * @param apply a consumer that accepts each player and their corresponding team
     * @see TeamAllocator
     */
    public void allocate(PlayerIterable players, BiConsumer<GameTeam, ServerPlayerEntity> apply) {
        var allocator = new TeamAllocator<GameTeam, ServerPlayerEntity>(this.teams.keySet());

        for (var entry : Reference2IntMaps.fastIterable(this.maxTeamSize)) {
            allocator.setSizeForTeam(entry.getKey(), entry.getIntValue());
        }

        for (var player : players) {
            GameTeam preference = this.teamPreference.get(player.getUuid());
            allocator.add(player, preference);
        }

        allocator.allocate(apply);
    }
}
