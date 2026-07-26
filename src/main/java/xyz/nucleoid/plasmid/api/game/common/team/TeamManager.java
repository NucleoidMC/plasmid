package xyz.nucleoid.plasmid.api.game.common.team;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.mixin.chat.PlayerListS2CPacketEntryAccessor;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/**
 * Simple, {@link GameActivity} specific team manager class.
 */
@SuppressWarnings({ "unused" })
public final class TeamManager implements Iterable<GameTeam> {
    private final Map<GameTeamKey, State> teamToState = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<UUID, GameTeamKey> playerToTeam = new Object2ObjectOpenHashMap<>();

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
     * @param key an identifier for the team to add
     * @param config the configuration for the given team
     * @return {@code true} if team is registered for the first time
     */
    public boolean addTeam(GameTeamKey key, GameTeamConfig config) {
        return this.addTeam(new GameTeam(key, config));
    }

    /**
     * Registers a team to this {@link TeamManager}.
     * Note that attempting to use an unregistered team will throw an exception!
     *
     * @param team the {@link GameTeam} to add
     * @return {@code true} if team is registered for the first time
     */
    public boolean addTeam(GameTeam team) {
        return this.teamToState.putIfAbsent(team.key(), new State(team)) == null;
    }

    /**
     * Registers a collection of teams to this {@link TeamManager}.
     * Note that attempting to use an unregistered team will throw an exception!
     *
     * @param teams the collection of teams to add
     */
    public void addTeams(GameTeamList teams) {
        teams.forEach(this::addTeam);
    }

    /**
     * Updates the {@link GameTeamConfig} associated with the given {@link GameTeamKey}.
     * These changes will then be synced to players and applied immediately.
     *
     * @param team the {@link GameTeamKey} to modify
     * @param config the new {@link GameTeamConfig} to apply to this team
     */
    public void setTeamConfig(GameTeamKey team, GameTeamConfig config) {
        this.teamState(team).setConfig(config);
        this.sendTeamUpdates(team);
    }

    /**
     * Gets the associated {@link GameTeamConfig} for the given {@link GameTeamKey}.
     * Attempting to access a team that is not registered will throw an exception!
     *
     * @param team the team to query
     * @return the associated {@link GameTeamConfig}
     */
    public GameTeamConfig getTeamConfig(GameTeamKey team) {
        return this.teamState(team).team.config();
    }

    /**
     * Adds given player to the given team, and removes them from any previous team they were apart of.
     *
     * @param player {@link PlayerRef} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(PlayerRef player, GameTeamKey team) {
        var lastTeam = this.playerToTeam.get(player.id());
        if (lastTeam == team) {
            return false;
        }

        if (lastTeam != null) {
            this.removePlayerFrom(player, lastTeam);
        }

        this.playerToTeam.put(player.id(), team);
        for (var gameSpacePlayer : gameSpace.getPlayers()) {
            this.sendTeamsToPlayer(gameSpacePlayer);
        }

        var state = this.teamState(team);
        if (state.allPlayers.add(player)) {
            var entity = this.gameSpace.getPlayers().getEntity(player.id());
            if (entity != null) {
                this.addOnlinePlayer(entity, state);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds given player to the given team, and removes them from any previous team they were apart of.
     *
     * @param player {@link ServerPlayer} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(ServerPlayer player, GameTeamKey team) {
        return this.addPlayerTo(PlayerRef.of(player), team);
    }

    /**
     * Removes the given player from the given team.
     *
     * @param player the {@link ServerPlayer} of the player to remove
     * @param team the team to be removed from
     * @return {@code true} if the player was removed from this team
     */
    public boolean removePlayerFrom(ServerPlayer player, GameTeamKey team) {
        return this.removePlayerFrom(PlayerRef.of(player), team);
    }

    /**
     * Removes the given player from the given team.
     *
     * @param player the {@link PlayerRef} of the player to remove
     * @param team the team to be removed from
     * @return {@code true} if the player was removed from this team
     */
    public boolean removePlayerFrom(PlayerRef player, GameTeamKey team) {
        if (!this.playerToTeam.remove(player.id(), team)) {
            return false;
        }

        var state = this.teamState(team);
        if (!state.allPlayers.remove(player)) {
            throw new IllegalStateException("Player " + player + " was not in team " + team + ", but had a mapping");
        }

        var entity = state.onlinePlayers.getEntity(player.id());
        if (entity != null) {
            this.sendRemoveTeamsForPlayer(entity);
            this.removeOnlinePlayer(entity, state);
        }
        return true;
    }

    /**
     * Removes the given player from any team they are apart of.
     *
     * @param player the {@link ServerPlayer} of the player to remove
     * @return the team that the player was removed from, or {@code null}
     */
    @Nullable
    public GameTeamKey removePlayer(ServerPlayer player) {
        return this.removePlayer(PlayerRef.of(player));
    }

    /**
     * Removes the given player from any team they are apart of.
     *
     * @param player the {@link PlayerRef} of the player to remove
     * @return the team that the player was removed from, or {@code null}
     */
    @Nullable
    public GameTeamKey removePlayer(PlayerRef player) {
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
     * @return the player's {@link GameTeamKey} or {@code null}
     */
    @Nullable
    public GameTeamKey teamFor(PlayerRef player) {
        return this.playerToTeam.get(player.id());
    }

    /**
     * Returns the team that the given player is apart of.
     *
     * @param player the player to query
     * @return the player's {@link GameTeamKey} or {@code null}
     */
    @Nullable
    public GameTeamKey teamFor(ServerPlayer player) {
        return this.playerToTeam.get(player.getUUID());
    }

    /**
     * Gets the {@link PlayerSet} of all online players within the given team.
     *
     * @param team targeted {@link GameTeamKey}
     * @return a {@link PlayerSet} of all online players within the given team
     */
    public PlayerSet playersIn(GameTeamKey team) {
        return this.teamState(team).onlinePlayers;
    }

    /**
     * Gets the {@link Set<PlayerRef>} of all players (including offline!) within the given team.
     *
     * @param team targeted {@link GameTeamKey}
     * @return a {@link Set<PlayerRef>} of all players within the given team
     */
    public Set<PlayerRef> allPlayersIn(GameTeamKey team) {
        return this.teamState(team).allPlayers;
    }

    private Component formatPlayerName(ServerPlayer player, Component name) {
        var team = this.teamFor(player);
        if (team != null) {
            var config = this.teamState(team).team.config();
            var style = Style.EMPTY.applyFormat(config.chatFormatting());
            return Component.empty().append(config.prefix())
                    .append(name.copy().setStyle(style))
                    .append(config.suffix());
        }
        return name;
    }

    @Nullable
    public GameTeamKey getSmallestTeam() {
        GameTeamKey smallest = null;
        int count = Integer.MAX_VALUE;

        for (var state : this.teamToState.values()) {
            int size = state.onlinePlayers.size();
            if (size <= count) {
                smallest = state.team.key();
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
    private TeamManager.State teamState(GameTeamKey team) {
        return Preconditions.checkNotNull(this.teamToState.get(team), "unregistered team for " + team);
    }

    private void onAddPlayer(ServerPlayer player) {
        this.sendTeamsToPlayer(player);
        this.restoreFormerTeams(player);
    }

    private void restoreFormerTeams(ServerPlayer player) {
        var team = this.teamFor(player);
        if (team != null) {
            var state = this.teamState(team);
            this.addOnlinePlayer(player, state);
        }
    }

    private void onRemovePlayer(ServerPlayer player) {
        var team = this.teamFor(player);
        if (team != null) {
            var state = this.teamState(team);
            this.removeOnlinePlayer(player, state);
        }

        if (!player.hasDisconnected()) {
            this.sendRemoveTeamsForPlayer(player);
        }
    }

    private EventResult onDamagePlayer(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer attacker) {
            var playerTeam = this.teamFor(player);
            var attackerTeam = this.teamFor(attacker);

            if (playerTeam != null && playerTeam == attackerTeam && !player.equals(attacker) && !this.getTeamConfig(playerTeam).friendlyFire()) {
                return EventResult.DENY;
            }
        }

        return EventResult.PASS;
    }

    private Component onFormatDisplayName(ServerPlayer player, Component name, Component vanilla) {
        return this.applyNameFormatting ? this.formatPlayerName(player, name) : name;
    }

    private void sendTeamsToPlayer(ServerPlayer player) {
        for (var state : this.teamToState.values()) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(state.scoreboardTeam, true));
            for (var member : state.onlinePlayers) {
                player.connection.send(this.updatePlayerName(member));
            }
        }
    }

    private void sendRemoveTeamsForPlayer(ServerPlayer player) {
        for (var state : this.teamToState.values()) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(state.scoreboardTeam));

            for (var member : state.onlinePlayers) {
                player.connection.send(this.resetPlayerName(member));
            }
        }
    }

    private void addOnlinePlayer(ServerPlayer player, State state) {
        if (!state.allPlayers.contains(PlayerRef.of(player))) {
            throw new IllegalStateException("Tried to mark player " + player.getScoreboardName() + " as online in team " + state.team + ", but they are not in this team");
        }

        state.onlinePlayers.add(player);
        state.scoreboardTeam.getPlayers().add(player.getScoreboardName());

        this.sendPacketToAll(this.changePlayerTeam(player, state, ClientboundSetPlayerTeamPacket.Action.ADD));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void removeOnlinePlayer(ServerPlayer player, State state) {
        if (!state.onlinePlayers.remove(player)) {
            throw new IllegalStateException("Tried to mark player " + player.getScoreboardName() + " as offline in team " + state.team + ", but they were not online in this team");
        }
        state.scoreboardTeam.getPlayers().remove(player.getScoreboardName());

        this.sendPacketToAll(this.changePlayerTeam(player, state, ClientboundSetPlayerTeamPacket.Action.REMOVE));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void sendTeamUpdates(GameTeamKey gameTeamKey) {
        var state = this.teamState(gameTeamKey);
        this.sendPacketToAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(state.scoreboardTeam, true));
    }

    private ClientboundSetPlayerTeamPacket changePlayerTeam(ServerPlayer player, State team, ClientboundSetPlayerTeamPacket.Action operation) {
        return ClientboundSetPlayerTeamPacket.createPlayerPacket(team.scoreboardTeam, player.getGameProfile().name(), operation);
    }

    private ClientboundPlayerInfoUpdatePacket updatePlayerName(ServerPlayer player) {
        var packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);

        var entry = packet.entries().get(0);
        var name = player.getTabListDisplayName();
        if (name == null) {
            name = player.getName();
        }
        ((PlayerListS2CPacketEntryAccessor) (Object) entry).setDisplayName(this.formatPlayerName(player, name));

        return packet;
    }

    private ClientboundPlayerInfoUpdatePacket resetPlayerName(ServerPlayer player) {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
    }

    private void sendPacketToAll(Packet<?> packet) {
        this.gameSpace.getPlayers().sendPacket(packet);
    }

    @NotNull
    @Override
    public Iterator<GameTeam> iterator() {
        return Iterators.transform(this.teamToState.values().iterator(), state -> state.team);
    }

    final class State {
        final Set<PlayerRef> allPlayers;
        final MutablePlayerSet onlinePlayers;
        final PlayerTeam scoreboardTeam;

        GameTeam team;

        State(GameTeam team) {
            this.allPlayers = new ObjectOpenHashSet<>();
            this.onlinePlayers = new MutablePlayerSet(TeamManager.this.gameSpace.getServer());

            this.scoreboardTeam = new PlayerTeam(TeamManager.this.scoreboard, team.key().id());
            team.config().applyToScoreboard(this.scoreboardTeam);

            this.team = team;
        }

        public void setConfig(GameTeamConfig config) {
            this.team = this.team.withConfig(config);
            config.applyToScoreboard(this.scoreboardTeam);
        }
    }
}


