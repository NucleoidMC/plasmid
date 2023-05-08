package xyz.nucleoid.plasmid.game.common.team;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
     * Adds given player to the given team, and removes them from any previous team they were apart from.
     *
     * @param player {@link PlayerRef} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(PlayerRef player, GameTeamKey team) {
        var lastTeam = this.playerToTeam.put(player.id(), team);
        if (lastTeam == team) {
            return false;
        }

        if (lastTeam != null) {
            this.removePlayerFrom(player, lastTeam);
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
     * Adds given player to the given team, and removes them from any previous team they were apart from.
     *
     * @param player {@link ServerPlayerEntity} to add
     * @param team the team to add the player to
     * @return {@code true} if player was successfully added
     */
    public boolean addPlayerTo(ServerPlayerEntity player, GameTeamKey team) {
        return this.addPlayerTo(PlayerRef.of(player), team);
    }

    /**
     * Removes the given player from the given team.
     *
     * @param player the {@link ServerPlayerEntity} of the player to remove
     * @param team the team to be removed from
     * @return {@code true} if the player was removed from this team
     */
    public boolean removePlayerFrom(ServerPlayerEntity player, GameTeamKey team) {
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
        this.playerToTeam.remove(player.id(), team);

        var state = this.teamState(team);
        if (state.allPlayers.remove(player)) {
            var entity = this.gameSpace.getPlayers().getEntity(player.id());
            if (entity != null) {
                this.removeOnlinePlayer(entity, state);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given player from any team they are apart from.
     *
     * @param player the {@link ServerPlayerEntity} of the player to remove
     * @return the team that the player was removed from, or {@code null}
     */
    @Nullable
    public GameTeamKey removePlayer(ServerPlayerEntity player) {
        return this.removePlayer(PlayerRef.of(player));
    }

    /**
     * Removes the given player from any team they are apart from.
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
     * Returns the team that the given player is apart from.
     *
     * @param player the player to query
     * @return the player's {@link GameTeamKey} or {@code null}
     */
    @Nullable
    public GameTeamKey teamFor(PlayerRef player) {
        return this.playerToTeam.get(player.id());
    }

    /**
     * Returns the team that the given player is apart from.
     *
     * @param player the player to query
     * @return the player's {@link GameTeamKey} or {@code null}
     */
    @Nullable
    public GameTeamKey teamFor(ServerPlayerEntity player) {
        return this.playerToTeam.get(player.getUuid());
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

    private Text formatPlayerName(ServerPlayerEntity player, Text name) {
        var team = this.teamFor(player);
        if (team != null) {
            var config = this.teamState(team).team.config();
            var style = Style.EMPTY.withFormatting(config.chatFormatting());
            return Text.empty().append(config.prefix())
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

    private void onAddPlayer(ServerPlayerEntity player) {
        var team = this.teamFor(player);
        if (team != null) {
            var state = this.teamState(team);
            this.addOnlinePlayer(player, state);
        }

        this.sendTeamsToPlayer(player);
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        this.teamToState.values().forEach(state -> {
            if(state.onlinePlayers.contains(player))
                this.removeOnlinePlayer(player, state);
        });

        if (!player.isDisconnected()) {
            this.sendRemoveTeamsForPlayer(player);
        }
    }

    private ActionResult onDamagePlayer(ServerPlayerEntity player, DamageSource source, float amount) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            var playerTeam = this.teamFor(player);
            var attackerTeam = this.teamFor(attacker);

            boolean friendlyFire = this.getTeamConfig(playerTeam).friendlyFire();
            boolean indirectFriendlyFire = this.getTeamConfig(playerTeam).indirectFriendlyFire();
            boolean isProtectedFromDamageSource = false;

            if(source.isOf(DamageTypes.EXPLOSION)
                    || source.isOf(DamageTypes.PLAYER_EXPLOSION)
                    || source.isOf(DamageTypes.FIREWORKS)
                    || source.isOf(DamageTypes.FIREBALL)
                    || source.isOf(DamageTypes.INDIRECT_MAGIC))
                isProtectedFromDamageSource = !indirectFriendlyFire; //when blocked friendly fire could also save the attacker, as example from an explosion

            if(source.isOf(DamageTypes.PLAYER_ATTACK)
                    || source.isOf(DamageTypes.ARROW)
                    || source.isOf(DamageTypes.TRIDENT)
                    || source.isOf(DamageTypes.MAGIC)
                    || source.isOf(DamageTypes.FALLING_BLOCK)
                    || source.isOf(DamageTypes.FALLING_STALACTITE)
                    || source.isOf(DamageTypes.FALLING_ANVIL))
                isProtectedFromDamageSource = !friendlyFire; //case where the attack is direct


            if (playerTeam != null && playerTeam == attackerTeam && isProtectedFromDamageSource) {
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    private Text onFormatDisplayName(ServerPlayerEntity player, Text name, Text vanilla) {
        return this.applyNameFormatting ? this.formatPlayerName(player, name) : name;
    }

    private void sendTeamsToPlayer(ServerPlayerEntity player) {
        for (var state : this.teamToState.values()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(state.scoreboardTeam, true));
            for (var member : state.onlinePlayers) {
                player.networkHandler.sendPacket(this.updatePlayerName(member));
            }
        }
    }

    private void sendRemoveTeamsForPlayer(ServerPlayerEntity player) {
        for (var state : this.teamToState.entrySet()) {
            var data = state.getValue();

            player.networkHandler.sendPacket(TeamS2CPacket.updateRemovedTeam(data.scoreboardTeam));

            for (var member : data.onlinePlayers) {
                player.networkHandler.sendPacket(this.resetPlayerName(member));
            }
        }
    }

    private void addOnlinePlayer(ServerPlayerEntity player, State state) {
        state.onlinePlayers.add(player);
        state.scoreboardTeam.getPlayerList().add(player.getEntityName());

        this.sendPacketToAll(this.changePlayerTeam(player, state, TeamS2CPacket.Operation.ADD));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void removeOnlinePlayer(ServerPlayerEntity player, State state) {
        state.onlinePlayers.remove(player);
        state.scoreboardTeam.getPlayerList().remove(player.getEntityName());

        this.sendPacketToAll(this.changePlayerTeam(player, state, TeamS2CPacket.Operation.REMOVE));
        this.sendPacketToAll(this.resetPlayerName(player));
    }

    private void sendTeamUpdates(GameTeamKey gameTeamKey) {
        var state = this.teamState(gameTeamKey);
        this.sendPacketToAll(TeamS2CPacket.updateTeam(state.scoreboardTeam, true));
    }

    private TeamS2CPacket changePlayerTeam(ServerPlayerEntity player, State team, TeamS2CPacket.Operation operation) {
        return TeamS2CPacket.changePlayerTeam(team.scoreboardTeam, player.getGameProfile().getName(), operation);
    }

    private PlayerListS2CPacket updatePlayerName(ServerPlayerEntity player) {
        var packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);

        var entry = packet.getEntries().get(0);
        var name = player.getPlayerListName();
        if (name == null) {
            name = player.getName();
        }
        ((PlayerListS2CPacketEntryAccessor) (Object) entry).setDisplayName(this.formatPlayerName(player, name));

        return packet;
    }

    private PlayerListS2CPacket resetPlayerName(ServerPlayerEntity player) {
        return new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
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
        final Team scoreboardTeam;

        GameTeam team;

        State(GameTeam team) {
            this.allPlayers = new ObjectOpenHashSet<>();
            this.onlinePlayers = new MutablePlayerSet(TeamManager.this.gameSpace.getServer());

            this.scoreboardTeam = new Team(TeamManager.this.scoreboard, team.key().id());
            team.config().applyToScoreboard(this.scoreboardTeam);

            this.team = team;
        }

        public void setConfig(GameTeamConfig config) {
            this.team = this.team.withConfig(config);
            config.applyToScoreboard(this.scoreboardTeam);
        }
    }
}


