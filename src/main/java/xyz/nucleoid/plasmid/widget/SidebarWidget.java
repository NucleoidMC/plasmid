package xyz.nucleoid.plasmid.widget;

import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Arrays;

public final class SidebarWidget implements PlayerSet.Listener, AutoCloseable {
    private static final int SIDEBAR_SLOT = 1;
    private static final int ADD_OBJECTIVE = 0;
    private static final int REMOVE_OBJECTIVE = 1;

    private static final String OBJECTIVE_NAME = Plasmid.ID + ":sidebar";

    private final PlayerSet players;
    private final Text title;

    private String[] display = new String[0];

    private SidebarWidget(Text title, PlayerSet players) {
        this.players = players;
        this.title = title;
        this.players.addListener(this);
    }

    public static SidebarWidget open(Text title, PlayerSet players) {
        SidebarWidget widget = new SidebarWidget(title, players);
        for (ServerPlayerEntity player : players) {
            widget.onAddPlayer(player);
        }

        return widget;
    }

    public void set(String[] display) {
        if (Arrays.equals(this.display, display)) {
            return;
        }

        // clear old lines
        for (int i = 0; i < this.display.length; i++) {
            int score = display.length - i;
            if (i >= display.length || !this.display[i].equals(display[i])) {
                this.players.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                        ServerScoreboard.UpdateMode.REMOVE, null,
                        this.display[i], score
                ));
            }
        }

        this.display = display;

        for (ServerPlayerEntity player : this.players) {
            this.sendDisplay(player, display);
        }
    }

    @Override
    public void onAddPlayer(ServerPlayerEntity player) {
        ScoreboardObjective objective = this.createDummyObjective();

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ADD_OBJECTIVE));
        player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(SIDEBAR_SLOT, objective));

        this.sendDisplay(player, this.display);
    }

    @Override
    public void onRemovePlayer(ServerPlayerEntity player) {
        ScoreboardObjective objective = this.createDummyObjective();
        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, REMOVE_OBJECTIVE));
    }

    private void sendDisplay(ServerPlayerEntity player, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int score = lines.length - i;
            player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                    ServerScoreboard.UpdateMode.CHANGE, OBJECTIVE_NAME,
                    line, score
            ));
        }
    }

    private ScoreboardObjective createDummyObjective() {
        return new ScoreboardObjective(
                null, OBJECTIVE_NAME,
                ScoreboardCriterion.DUMMY,
                this.title,
                ScoreboardCriterion.RenderType.INTEGER
        );
    }

    @Override
    public void close() {
        this.players.removeListener(this);

        for (ServerPlayerEntity player : this.players) {
            this.onRemovePlayer(player);
        }
    }
}
