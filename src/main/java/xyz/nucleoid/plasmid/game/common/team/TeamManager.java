package xyz.nucleoid.plasmid.game.common.team;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
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
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.*;

/**
 * Simple, GameActivity specific team manager class
 */
public final class TeamManager {
    private final Map<GameTeam, Set<PlayerRef>> teamToPlayerRefSet = new Object2ObjectOpenHashMap<>();
    private final Map<GameTeam, MutablePlayerSet> teamToPlayerSet = new Object2ObjectOpenHashMap<>();
    private final Map<PlayerRef, GameTeam> playerRefToTeam = new Object2ObjectOpenHashMap<>();
    private final Map<GameTeam, Team> teamToVanillaTeam = new Object2ObjectOpenHashMap<>();
    private final Object2BooleanMap<GameTeam> teamFriendlyFire = new Object2BooleanArrayMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final MinecraftServer server;
    private GameActivity gameActivity;
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
        this.gameActivity = activity;

        activity.listen(GamePlayerEvents.ADD, (player) -> {
            for (Team vanillaTeam : this.teamToVanillaTeam.values()) {
                player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(vanillaTeam, true));
            }

            PlayerRef ref = PlayerRef.of(player);
            GameTeam team = this.playerRefToTeam.get(ref);
            if (team != null) {
                this.teamToPlayerSet.computeIfAbsent(team, (t) -> new MutablePlayerSet(this.server)).add(ref);
            }
        });

        activity.listen(GamePlayerEvents.REMOVE, (player) -> {
            for (Team vanillaTeam : this.teamToVanillaTeam.values()) {
                player.networkHandler.sendPacket(TeamS2CPacket.updateRemovedTeam(vanillaTeam));
            }

            PlayerRef ref = PlayerRef.of(player);
            GameTeam team = this.playerRefToTeam.get(ref);
            if (team != null) {
                this.teamToPlayerSet.computeIfAbsent(team, (t) -> new MutablePlayerSet(this.server)).remove(ref);
            }
        });

        activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> {
            if (source.getAttacker() instanceof PlayerEntity attacker) {
                GameTeam playerTeam = this.playerRefToTeam.get(PlayerRef.of(player));
                GameTeam attackerTeam = this.playerRefToTeam.get(PlayerRef.of(attacker));

                if (playerTeam != null && playerTeam == attackerTeam && !this.teamFriendlyFire.getBoolean(playerTeam)) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        activity.listen(GamePlayerEvents.DISPLAY_NAME, (player, current, vanilla) -> {
            if (this.applyFormattingByDefault) {
                PlayerRef ref = PlayerRef.of(player);
                return this.applyPlayerFormatting(ref, current);
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
        if (this.teamToPlayerRefSet.computeIfAbsent(team, (t) -> new HashSet<>()).add(ref)) {
            this.teamToPlayerSet.computeIfAbsent(team, (t) -> new MutablePlayerSet(this.server)).add(ref);
            this.playerRefToTeam.put(ref, team);

            Team vanilla = this.getVanilla(team);
            vanilla.getPlayerList().add(ref.name());
            if (this.gameActivity != null) {
                Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, ref.name(), TeamS2CPacket.Operation.ADD);
                this.gameActivity.getGameSpace().getPlayers().sendPacket(packet);
            }

            return true;
        }
        return false;
    }

    /**
     * Adds player to the team by PlayerRef
     *
     * @param player
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
        GameTeam team = this.playerRefToTeam.remove(ref);
        if (team != null) {
            this.teamToPlayerRefSet.get(team).remove(ref);
            this.teamToPlayerSet.get(team).remove(ref);

            Team vanilla = this.getVanilla(team);
            vanilla.getPlayerList().remove(ref.name());
            if (this.gameActivity != null) {
                Packet<?> packet = TeamS2CPacket.changePlayerTeam(vanilla, ref.name(), TeamS2CPacket.Operation.REMOVE);
                this.gameActivity.getGameSpace().getPlayers().sendPacket(packet);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes player from team
     *
     * @param player
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
        return this.playerRefToTeam.get(player);
    }

    /**
     * Returns team player is in
     *
     * @param player
     * @return GameTeam or null
     */
    @Nullable
    public GameTeam getTeamOf(ServerPlayerEntity player) {
        return this.playerRefToTeam.get(PlayerRef.of(player));
    }

    /**
     * Gets set of players being part of the team
     *
     * @param team targeted {@link GameTeam}
     * @return Set of Players
     */
    public PlayerSet getPlayers(GameTeam team) {
        PlayerSet set = this.teamToPlayerSet.get(team);

        return set == null ? PlayerSet.EMPTY : set;
    }

    /**
     * Gets set of {@link PlayerRef} being part of the team
     *
     * @param team targeted {@link GameTeam}
     * @return Set of {@link PlayerRef}
     */
    public Set<PlayerRef> getPlayerRefs(GameTeam team) {
        Set<PlayerRef> set = this.teamToPlayerRefSet.get(team);

        return set == null ? Collections.emptySet() : set;
    }

    public boolean getFriendlyFire(GameTeam team) {
        return this.teamFriendlyFire.getBoolean(team);
    }

    public boolean setFriendlyFire(GameTeam team, boolean value) {
        return this.teamFriendlyFire.put(team, value);
    }

    public AbstractTeam.CollisionRule getCollisionRule(GameTeam team) {
        return this.getVanilla(team).getCollisionRule();
    }

    public void setCollisionRule(GameTeam team, AbstractTeam.CollisionRule rule) {
        this.getVanilla(team).setCollisionRule(rule);
        this.sendUpdates(team);
    }

    public AbstractTeam.VisibilityRule getNameTagVisibilityRule(GameTeam team) {
        return this.getVanilla(team).getNameTagVisibilityRule();
    }

    public void setNameTagVisibilityRule(GameTeam team, AbstractTeam.VisibilityRule rule) {
        this.getVanilla(team).setNameTagVisibilityRule(rule);
        this.sendUpdates(team);
    }

    public Text applyPlayerFormatting(PlayerRef player, Text text) {
        GameTeam team = this.playerRefToTeam.get(player);
        if (team != null) {
            Team vanillaTeam = this.getVanilla(team);
            return new LiteralText("").append(vanillaTeam.getPrefix()).append(text.shallowCopy().setStyle(Style.EMPTY.withColor(team.color()))).append(vanillaTeam.getSuffix());
        }
        return text;
    }

    public Text getPrefix(GameTeam team) {
        return this.getVanilla(team).getPrefix();
    }

    public void setPrefix(GameTeam team, Text value) {
        this.getVanilla(team).setPrefix(value);
        this.sendUpdates(team);
    }

    public Text getDisplayName(GameTeam team) {
        return this.getVanilla(team).getDisplayName();
    }

    public void setDisplayName(GameTeam team, Text value) {
        this.getVanilla(team).setDisplayName(value);
        this.sendUpdates(team);
    }

    public Text getSuffix(GameTeam team) {
        return this.getVanilla(team).getSuffix();
    }

    public void setSuffix(GameTeam team, Text value) {
        this.getVanilla(team).setSuffix(value);
        this.sendUpdates(team);
    }

    public GameTeam getSmallestTeam() {
        GameTeam smallest = null;
        int count = 9999;

        for (var entry : this.teamToPlayerRefSet.entrySet()) {
            int size = entry.getValue().size();
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
     * Pre-registers team, allowing methods like {@link TeamManager#getSmallestTeam()} to work correctly
     *
     * @param team targeted team
     * @return true if team is registered for the first time
     */
    public boolean registerTeam(GameTeam team) {
        if (!this.teamToPlayerRefSet.containsKey(team)) {
            this.teamToPlayerRefSet.put(team, new HashSet<>());
            this.getVanilla(team);
            return true;
        }
        return false;
    }

    private Team getVanilla(GameTeam gameTeam) {
        return this.teamToVanillaTeam.computeIfAbsent(gameTeam, (t) -> {
            Team team = new Team(this.scoreboard, gameTeam.key());
            team.setColor(gameTeam.formatting());
            return team;
        });
    }

    private void sendUpdates(GameTeam gameTeam) {
        if (this.gameActivity != null) {
            this.gameActivity.getGameSpace().getPlayers().sendPacket(TeamS2CPacket.updateTeam(this.getVanilla(gameTeam), true));
        }
    }
}


