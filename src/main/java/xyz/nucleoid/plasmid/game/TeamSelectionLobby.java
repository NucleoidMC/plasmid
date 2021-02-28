package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.TeamAllocator;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class TeamSelectionLobby {
    private static final String TEAM_KEY = Plasmid.ID + ":team";

    private final GameSpace gameSpace;
    private final Map<String, GameTeam> teams;

    private final Reference2IntMap<GameTeam> maxTeamSize = new Reference2IntOpenHashMap<>();
    private final Map<UUID, GameTeam> teamPreference = new Object2ObjectOpenHashMap<>();

    private TeamSelectionLobby(GameSpace gameSpace, Map<String, GameTeam> teams) {
        this.gameSpace = gameSpace;
        this.teams = teams;
    }

    public void setSizeForTeam(GameTeam team, int size) {
        this.maxTeamSize.put(team, size);
    }

    public static TeamSelectionLobby applyTo(GameLogic logic, Collection<GameTeam> teams) {
        Map<String, GameTeam> teamMap = new Object2ObjectOpenHashMap<>();
        for (GameTeam team : teams) {
            teamMap.put(team.getKey(), team);
        }

        TeamSelectionLobby lobby = new TeamSelectionLobby(logic.getSpace(), teamMap);

        logic.on(PlayerAddListener.EVENT, lobby::onAddPlayer);
        logic.on(UseItemListener.EVENT, lobby::onUseItem);

        return lobby;
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        int index = 0;

        for (GameTeam team : this.teams.values()) {
            Text name = new TranslatableText("text.plasmid.team_selection.request_team", team.getDisplay())
                    .formatted(Formatting.BOLD, team.getFormatting());

            ItemStack stack = new ItemStack(ColoredBlocks.wool(team.getDye()));
            stack.setCustomName(name);

            stack.getOrCreateTag().putString(TEAM_KEY, team.getKey());

            player.inventory.setStack(index++, stack);
        }
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem().isIn(ItemTags.WOOL)) {
            CompoundTag tag = stack.getOrCreateTag();
            String teamKey = tag.getString(TEAM_KEY);

            GameTeam team = this.teams.get(teamKey);
            if (team != null) {
                this.teamPreference.put(player.getUuid(), team);

                Text message = new TranslatableText("text.plasmid.team_selection.requested_team",
                        new TranslatableText("text.plasmid.team_selection.suffixed_team", team.getDisplay()).formatted(team.getFormatting()));

                player.sendMessage(message, false);

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    public void allocate(BiConsumer<GameTeam, ServerPlayerEntity> apply) {
        TeamAllocator<GameTeam, ServerPlayerEntity> allocator = new TeamAllocator<>(this.teams.values());

        for (Reference2IntMap.Entry<GameTeam> entry : Reference2IntMaps.fastIterable(this.maxTeamSize)) {
            allocator.setSizeForTeam(entry.getKey(), entry.getIntValue());
        }

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            GameTeam preference = this.teamPreference.get(player.getUuid());
            allocator.add(player, preference);
        }

        allocator.allocate(apply);
    }
}
