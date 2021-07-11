package xyz.nucleoid.plasmid.game.common.team;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.*;

/**
 * Simple, {@link GameActivity} specific team manager class
 */
@SuppressWarnings({ "unused" })
public final class TeamManager {
    private final Map<GameTeam, TeamData> teams = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, GameTeam> uuidToTeam = new Object2ObjectOpenHashMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final MinecraftServer server;
    private GameSpace gameSpace = null;
    private boolean applyFormattingByDefault = true;

    public TeamManager(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Applies this TeamManager to the given {@link GameActivity}.
     *
     * @param activity the activity to add to
     */
    public void applyTo(GameActivity activity) {
        this.gameSpace = activity.getGameSpace();

        activity.listen(GameActivityEvents.DESTROY, (reason) -> this.gameSpace = null);

        activity.listen(GamePlayerEvents.ADD, (player) -> {
            GameTeam team = this.uuidToTeam.get(player.getUuid());
            if (team != null) {
                TeamData data = this.teams.get(team);
                data.activePlayers.add(player);

                Team vanilla = data.vanillaTeam;
                vanilla.getPlayerList().add(player.getGameProfile().getName());
                if (this.gameSpace != null) {
                    Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, player.getGameProfile().getName(), TeamS2CPacket.Operation.ADD);
                    this.gameSpace.getPlayers().sendPacket(packet);
                }
            }

            for (TeamData data : this.teams.values()) {
                player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(data.vanillaTeam, true));
            }
        });

        activity.listen(GamePlayerEvents.REMOVE, (player) -> {
            if (!player.isDisconnected()) {
                for (TeamData data : this.teams.values()) {
                    player.networkHandler.sendPacket(TeamS2CPacket.updateRemovedTeam(data.vanillaTeam));
                }
            }

            GameTeam team = this.uuidToTeam.get(player.getUuid());
            if (team != null) {
                TeamData data = this.teams.get(team);
                data.activePlayers.remove(player);

                Team vanilla = data.vanillaTeam;
                vanilla.getPlayerList().remove(player.getGameProfile().getName());
                if (this.gameSpace != null) {
                    Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, player.getGameProfile().getName(), TeamS2CPacket.Operation.REMOVE);
                    this.gameSpace.getPlayers().sendPacket(packet);
                }
            }
        });

        activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
                GameTeam playerTeam = this.getTeamOf(player);
                GameTeam attackerTeam = this.getTeamOf(attacker);

                if (playerTeam != null && playerTeam == attackerTeam && !this.teams.get(playerTeam).friendlyFire) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        activity.listen(GamePlayerEvents.DISPLAY_NAME, (player, current, vanilla) -> {
            if (this.applyFormattingByDefault) {
                return this.formatPlayerName(player, current);
            }
            return current;
        });
    }

    /**
     * Adds player to the team by PlayerRef
     *
     * @param ref {@link PlayerRef} of player
     * @param team team player is added to
     * @return true if player was successfully added
     */
    public boolean setPlayerTeam(PlayerRef ref, GameTeam team) {
        this.removePlayerTeam(ref);
        TeamData data = this.teams.get(team);
        if (data != null && data.players.add(ref)) {
            data.activePlayers.add(ref);
            this.uuidToTeam.put(ref.id(), team);

            if (this.gameSpace != null && this.gameSpace.getPlayers().contains(ref)) {
                ServerPlayerEntity player = ref.getEntity(this.server);
                if (player != null) {
                    Team vanilla = this.teams.get(team).vanillaTeam;
                    vanilla.getPlayerList().add(player.getGameProfile().getName());
                    if (this.gameSpace != null) {
                        Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, player.getGameProfile().getName(), TeamS2CPacket.Operation.ADD);
                        this.gameSpace.getPlayers().sendPacket(packet);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds player to the team by PlayerRef
     *
     * @param player a player reference
     * @param team team player is added to
     * @return true if player was successfully added
     */
    public boolean setPlayerTeam(ServerPlayerEntity player, GameTeam team) {
        return this.setPlayerTeam(PlayerRef.of(player), team);
    }

    /**
     * Removes player from team
     *
     * @param ref {@link PlayerRef} of player
     * @return true if player was removed successfully
     */
    public boolean removePlayerTeam(PlayerRef ref) {
        GameTeam team = this.uuidToTeam.get(ref.id());
        if (team != null) {
            TeamData data = this.teams.get(team);
            data.activePlayers.remove(ref);
            data.players.remove(ref);
            if (this.gameSpace != null && this.gameSpace.getPlayers().contains(ref)) {
                ServerPlayerEntity player = ref.getEntity(this.server);
                if (player != null) {
                    Team vanilla = this.teams.get(team).vanillaTeam;
                    vanilla.getPlayerList().add(player.getGameProfile().getName());
                    if (this.gameSpace != null) {
                        Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, player.getGameProfile().getName(), TeamS2CPacket.Operation.REMOVE);
                        this.gameSpace.getPlayers().sendPacket(packet);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Removes player from team
     *
     * @param player a player reference
     * @return true if player was removed successfully
     */
    public boolean removePlayerTeam(ServerPlayerEntity player) {
        return this.removePlayerTeam(PlayerRef.of(player));
    }

    /**
     * Returns team player is in
     *
     * @param player Player reference
     * @return GameTeam or null
     */
    @Nullable
    public GameTeam getTeamOf(PlayerRef player) {
        return this.uuidToTeam.get(player.id());
    }

    /**
     * Returns team player is in
     *
     * @param player a player reference
     * @return GameTeam or null
     */
    @Nullable
    public GameTeam getTeamOf(ServerPlayerEntity player) {
        return this.uuidToTeam.get(player.getUuid());
    }

    /**
     * Gets set of players being part of the team
     *
     * @param team targeted {@link GameTeam}
     * @return Set of Players
     */
    public PlayerSet getPlayers(GameTeam team) {
        TeamData data = this.teams.get(team);
        return data != null ? data.activePlayers : PlayerSet.EMPTY;
    }

    /**
     * Gets set of {@link PlayerRef} being part of the team
     *
     * @param team targeted {@link GameTeam}
     * @return Set of {@link PlayerRef}
     */
    public Set<PlayerRef> getPlayerRefs(GameTeam team) {
        TeamData data = this.teams.get(team);
        return data != null ? data.players : Collections.emptySet();
    }

    public boolean getFriendlyFire(GameTeam team) {
        return this.teams.get(team).friendlyFire;
    }

    public boolean setFriendlyFire(GameTeam team, boolean value) {
        return this.teams.get(team).friendlyFire = value;
    }

    public AbstractTeam.CollisionRule getCollisionRule(GameTeam team) {
        return this.teams.get(team).vanillaTeam.getCollisionRule();
    }

    public void setCollisionRule(GameTeam team, AbstractTeam.CollisionRule rule) {
        this.teams.get(team).vanillaTeam.setCollisionRule(rule);
        this.sendUpdates(team);
    }

    public AbstractTeam.VisibilityRule getNameTagVisibilityRule(GameTeam team) {
        return this.teams.get(team).vanillaTeam.getNameTagVisibilityRule();
    }

    public void setNameTagVisibilityRule(GameTeam team, AbstractTeam.VisibilityRule rule) {
        this.teams.get(team).vanillaTeam.setNameTagVisibilityRule(rule);
        this.sendUpdates(team);
    }

    public Text formatPlayerName(ServerPlayerEntity player, Text text) {
        GameTeam team = this.getTeamOf(player);
        if (team != null) {
            Team vanillaTeam = this.teams.get(team).vanillaTeam;
            return new LiteralText("").append(vanillaTeam.getPrefix()).append(text.shallowCopy().setStyle(Style.EMPTY.withColor(team.color()))).append(vanillaTeam.getSuffix());
        }
        return text;
    }

    public Text getPrefix(GameTeam team) {
        return this.teams.get(team).vanillaTeam.getPrefix();
    }

    public void setPrefix(GameTeam team, Text value) {
        this.teams.get(team).vanillaTeam.setPrefix(value);
        this.sendUpdates(team);
    }

    public Text getDisplayName(GameTeam team) {
        return this.teams.get(team).vanillaTeam.getDisplayName();
    }

    public void setDisplayName(GameTeam team, Text value) {
        this.teams.get(team).vanillaTeam.setDisplayName(value);
        this.sendUpdates(team);
    }

    public Text getSuffix(GameTeam team) {
        return this.teams.get(team).vanillaTeam.getSuffix();
    }

    public void setSuffix(GameTeam team, Text value) {
        this.teams.get(team).vanillaTeam.setSuffix(value);
        this.sendUpdates(team);
    }

    @Nullable
    public GameTeam getSmallestTeam() {
        GameTeam smallest = null;
        int count = Integer.MAX_VALUE;

        for (var entry : this.teams.entrySet()) {
            int size = entry.getValue().players.size();
            if (size <= count) {
                smallest = entry.getKey();
                count = size;
            }
        }

        return smallest;
    }

    public void disableFormatting() {
        this.applyFormattingByDefault = false;
    }

    public void enableFormatting() {
        this.applyFormattingByDefault = true;
    }

    /**
     * Registers a team.
     * Unregistered teams won't work
     *
     * @param team targeted team
     * @return true if team is registered for the first time
     */
    public boolean registerTeam(GameTeam team) {
        if (!this.teams.containsKey(team)) {
            this.teams.put(team, new TeamData(team));
            return true;
        }
        return false;
    }

    private void sendUpdates(GameTeam gameTeam) {
        TeamData data = this.teams.get(gameTeam);
        if (data != null && this.gameSpace != null) {
            this.gameSpace.getPlayers().sendPacket(TeamS2CPacket.updateTeam(data.vanillaTeam, true));
        }
    }

    private class TeamData {
        boolean friendlyFire = false;
        Team vanillaTeam;
        MutablePlayerSet activePlayers;
        Set<PlayerRef> players;

        TeamData(GameTeam team) {
            this.vanillaTeam = new Team(TeamManager.this.scoreboard, team.key());
            this.vanillaTeam.setColor(team.formatting());
            this.activePlayers = new MutablePlayerSet(TeamManager.this.server);
            this.players = new HashSet<>();
        }
    }
}


