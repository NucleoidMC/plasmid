package xyz.nucleoid.plasmid.game.common.team;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.mixin.chat.PlayerListS2CPacketEntryAccessor;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Simple, {@link GameActivity} specific team manager class.
 */
@SuppressWarnings({ "unused" })
public final class TeamManager {
    private final Map<GameTeam, Entry> teams = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, GameTeam> playerToTeam = new Object2ObjectOpenHashMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final GameSpace gameSpace;

    private boolean applyNameFormatting = true;

    private TeamManager(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    /**
     * Creates and applies a {@link TeamManager} instance to the given {@link GameActivity}.
     *
     * @param activity the activity to apply teams to
     * @return the constructed {@link TeamManager}
     */
    public static TeamManager addTo(GameActivity activity) {
        var manager = new TeamManager(activity.getGameSpace());
        activity.listen(GamePlayerEvents.ADD, manager::onAddPlayer);
        activity.listen(GamePlayerEvents.REMOVE, manager::onRemovePlayer);
        activity.listen(PlayerDamageEvent.EVENT, manager::onDamagePlayer);
        activity.listen(GamePlayerEvents.DISPLAY_NAME, manager::onFormatDisplayName);
        return manager;
    }

    /**
     * Registers a team to this {@link TeamManager}.
     * Note that attempting to use an unregistered team will throw an exception!
     *
     * @param team the {@link GameTeam} to add
     * @return {@code true} if team is registered for the first time
     */
    public boolean addTeam(GameTeam team) {
        return this.teams.putIfAbsent(team, new Entry(team)) == null;
    }

    /**
     * Adds given player to the given team, and removes them from any previous team they were apart of.
     *
     * @param player {@link PlayerRef} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(PlayerRef player, GameTeam team) {
        var lastTeam = this.playerToTeam.put(player.id(), team);
        if (lastTeam == team) {
            return false;
        }

        if (lastTeam != null) {
            this.removePlayerFrom(player, lastTeam);
        }

        var entry = this.teamEntry(team);
        if (entry.allPlayers.add(player)) {
            var entity = this.gameSpace.getPlayers().getEntity(player.id());
            if (entity != null) {
                this.addOnlinePlayer(entity, entry);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds given player to the given team, and removes them from any previous team they were apart of.
     *
     * @param player {@link ServerPlayerEntity} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(ServerPlayerEntity player, GameTeam team) {
        return this.addPlayerTo(PlayerRef.of(player), team);
    }

    /**
     * Removes the given player from the given team.
     *
     * @param player the {@link ServerPlayerEntity} of the player to remove
     * @param team the team to be removed from
     * @return {@code true} if the player was removed from this team
     */
    public boolean removePlayerFrom(ServerPlayerEntity player, GameTeam team) {
        return this.removePlayerFrom(PlayerRef.of(player), team);
    }

    /**
     * Removes the given player from the given team.
     *
     * @param player the {@link PlayerRef} of the player to remove
     * @param team the team to be removed from
     * @return {@code true} if the player was removed from this team
     */
    public boolean removePlayerFrom(PlayerRef player, GameTeam team) {
        this.playerToTeam.remove(player.id(), team);

        var entry = this.teamEntry(team);
        if (entry.allPlayers.remove(player)) {
            var entity = this.gameSpace.getPlayers().getEntity(player.id());
            if (entity != null) {
                this.removeOnlinePlayer(entity, entry);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given player from any team they are apart of.
     *
     * @param player the {@link ServerPlayerEntity} of the player to remove
     * @return the team that the player was removed from, or {@code null}
     */
    @Nullable
    public GameTeam removePlayer(ServerPlayerEntity player) {
        return this.removePlayer(PlayerRef.of(player));
    }

    /**
     * Removes the given player from any team they are apart of.
     *
     * @param player the {@link PlayerRef} of the player to remove
     * @return the team that the player was removed from, or {@code null}
     */
    @Nullable
    public GameTeam removePlayer(PlayerRef player) {
        var team = this.teamFor(player);
        if (team != null) {
            this.removePlayerFrom(player, team);
        }
        return team;
    }

    /**
     * Returns the team that the given player is apart of.
     *
     * @param player the player to query
     * @return the player's {@link GameTeam} or {@code null}
     */
    @Nullable
    public GameTeam teamFor(PlayerRef player) {
        return this.playerToTeam.get(player.id());
    }

    /**
     * Returns the team that the given player is apart of.
     *
     * @param player the player to query
     * @return the player's {@link GameTeam} or {@code null}
     */
    @Nullable
    public GameTeam teamFor(ServerPlayerEntity player) {
        return this.playerToTeam.get(player.getUuid());
    }

    /**
     * Gets the {@link PlayerSet} of all online players within the given team.
     *
     * @param team targeted {@link GameTeam}
     * @return a {@link PlayerSet} of all online players within the given team
     */
    public PlayerSet playersIn(GameTeam team) {
        return this.teamEntry(team).onlinePlayers;
    }

    /**
     * Gets the {@link Set<PlayerRef>} of all players (including offline!) within the given team.
     *
     * @param team targeted {@link GameTeam}
     * @return a {@link Set<PlayerRef>} of all players within the given team
     */
    public Set<PlayerRef> allPlayersIn(GameTeam team) {
        return this.teamEntry(team).allPlayers;
    }

    public boolean setFriendlyFire(GameTeam team, boolean value) {
        return this.teamEntry(team).friendlyFire = value;
    }

    public boolean hasFriendlyFire(GameTeam team) {
        return this.teamEntry(team).friendlyFire;
    }

    public void setCollisionRule(GameTeam team, AbstractTeam.CollisionRule rule) {
        this.teamEntry(team).scoreboardTeam.setCollisionRule(rule);
        this.sendTeamUpdates(team);
    }

    public AbstractTeam.CollisionRule getCollisionRule(GameTeam team) {
        return this.teamEntry(team).scoreboardTeam.getCollisionRule();
    }

    public void setNameTagVisibility(GameTeam team, AbstractTeam.VisibilityRule rule) {
        this.teamEntry(team).scoreboardTeam.setNameTagVisibilityRule(rule);
        this.sendTeamUpdates(team);
    }

    public AbstractTeam.VisibilityRule getNameTagVisibility(GameTeam team) {
        return this.teamEntry(team).scoreboardTeam.getNameTagVisibilityRule();
    }

    private Text formatPlayerName(ServerPlayerEntity player, Text name) {
        var team = this.teamFor(player);
        if (team != null) {
            var style = Style.EMPTY.withColor(team.color());
            var scoreboardTeam = this.teamEntry(team).scoreboardTeam;
            return new LiteralText("").append(scoreboardTeam.getPrefix()).append(name.shallowCopy().setStyle(style)).append(scoreboardTeam.getSuffix());
        }
        return name;
    }

    public void setPrefix(GameTeam team, Text prefix) {
        var entry = this.teamEntry(team);
        entry.prefix = prefix;
        entry.scoreboardTeam.setPrefix(prefix);

        this.sendTeamUpdates(team);
    }

    public Text getPrefix(GameTeam team) {
        return this.teamEntry(team).prefix;
    }

    public void setSuffix(GameTeam team, Text suffix) {
        var entry = this.teamEntry(team);
        entry.suffix = suffix;
        entry.scoreboardTeam.setSuffix(suffix);

        this.sendTeamUpdates(team);
    }

    public Text getSuffix(GameTeam team) {
        return this.teamEntry(team).suffix;
    }

    @Nullable
    public GameTeam getSmallestTeam() {
        GameTeam smallest = null;
        int count = Integer.MAX_VALUE;

        for (var entry : this.teams.entrySet()) {
            int size = entry.getValue().onlinePlayers.size();
            if (size <= count) {
                smallest = entry.getKey();
                count = size;
            }
        }

        return smallest;
    }

    public void enableNameFormatting() {
        this.applyNameFormatting = true;
    }

    public void disableNameFormatting() {
        this.applyNameFormatting = false;
    }

    @NotNull
    private Entry teamEntry(GameTeam team) {
        return Preconditions.checkNotNull(this.teams.get(team), "unregistered team for " + team);
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        var team = this.teamFor(player);
        if (team != null) {
            var entry = this.teamEntry(team);
            this.addOnlinePlayer(player, entry);
        }

        this.sendTeamsToPlayer(player);
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        var team = this.teamFor(player);
        if (team != null) {
            var entry = this.teamEntry(team);
            this.removeOnlinePlayer(player, entry);
        }

        if (!player.isDisconnected()) {
            this.sendRemoveTeamsForPlayer(player);
        }
    }

    private ActionResult onDamagePlayer(ServerPlayerEntity player, DamageSource source, float amount) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            var playerTeam = this.teamFor(player);
            var attackerTeam = this.teamFor(attacker);

            if (playerTeam != null && playerTeam == attackerTeam && !this.teamEntry(playerTeam).friendlyFire) {
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    private Text onFormatDisplayName(ServerPlayerEntity player, Text name, Text vanilla) {
        return this.applyNameFormatting ? this.formatPlayerName(player, name) : name;
    }

    private void sendTeamsToPlayer(ServerPlayerEntity player) {
        for (var entry : this.teams.values()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(entry.scoreboardTeam, true));
            for (var member : entry.onlinePlayers) {
                player.networkHandler.sendPacket(this.updatePlayerName(member));
            }
        }
    }

    private void sendRemoveTeamsForPlayer(ServerPlayerEntity player) {
        for (var entry : this.teams.entrySet()) {
            var data = entry.getValue();

            player.networkHandler.sendPacket(TeamS2CPacket.updateRemovedTeam(data.scoreboardTeam));

            for (var member : data.onlinePlayers) {
                player.networkHandler.sendPacket(this.resetPlayerName(member));
            }
        }
    }

    private void addOnlinePlayer(ServerPlayerEntity player, Entry entry) {
        entry.onlinePlayers.add(player);
        entry.scoreboardTeam.getPlayerList().add(player.getEntityName());

        this.sendPacketToAll(this.changePlayerTeam(player, entry, TeamS2CPacket.Operation.ADD));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void removeOnlinePlayer(ServerPlayerEntity player, Entry entry) {
        entry.onlinePlayers.remove(player);
        entry.scoreboardTeam.getPlayerList().remove(player.getEntityName());

        this.sendPacketToAll(this.changePlayerTeam(player, entry, TeamS2CPacket.Operation.REMOVE));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void sendTeamUpdates(GameTeam gameTeam) {
        var entry = this.teamEntry(gameTeam);
        this.sendPacketToAll(TeamS2CPacket.updateTeam(entry.scoreboardTeam, true));
    }

    private TeamS2CPacket changePlayerTeam(ServerPlayerEntity player, Entry team, TeamS2CPacket.Operation operation) {
        return TeamS2CPacket.changePlayerTeam(team.scoreboardTeam, player.getGameProfile().getName(), operation);
    }

    private PlayerListS2CPacket updatePlayerName(ServerPlayerEntity player) {
        var packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);

        var entry = packet.getEntries().get(0);
        var name = player.getPlayerListName();
        if (name == null) {
            name = player.getName();
        }
        ((PlayerListS2CPacketEntryAccessor) entry).setDisplayName(this.formatPlayerName(player, name));

        return packet;
    }

    private PlayerListS2CPacket resetPlayerName(ServerPlayerEntity player) {
        return new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
    }

    private void sendPacketToAll(Packet<?> packet) {
        this.gameSpace.getPlayers().sendPacket(packet);
    }

    final class Entry {
        final Set<PlayerRef> allPlayers;
        final MutablePlayerSet onlinePlayers;
        final Team scoreboardTeam;

        boolean friendlyFire = false;
        Text prefix = LiteralText.EMPTY;
        Text suffix = LiteralText.EMPTY;

        Entry(GameTeam team) {
            this.allPlayers = new ObjectOpenHashSet<>();
            this.onlinePlayers = new MutablePlayerSet(TeamManager.this.gameSpace.getServer());

            this.scoreboardTeam = new Team(TeamManager.this.scoreboard, team.key());
            this.scoreboardTeam.setColor(team.formatting());
        }
    }
}


