package xyz.nucleoid.plasmid.widget;

import fr.catcore.server.translations.api.LocalizableText;
import fr.catcore.server.translations.api.LocalizationTarget;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SidebarWidget implements PlayerSet.Listener, AutoCloseable {
    private static final int SIDEBAR_SLOT = 1;
    private static final int ADD_OBJECTIVE = 0;
    private static final int REMOVE_OBJECTIVE = 1;

    private static final String OBJECTIVE_NAME = Plasmid.ID + ":sidebar";

    private final PlayerSet players;
    private final Text title;

    private Text[] display = new Text[0];

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

    /*
       Style wont work.
     */
    public void set(TranslatableText[] display) {
        if (Arrays.equals(this.display, display)) {
            return;
        }

        // clear old lines
        for (int i = 0; i < this.display.length; i++) {
            int score = display.length - i;
            if (i >= display.length || !this.display[i].equals(display[i])) {
                this.players.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                        ServerScoreboard.UpdateMode.REMOVE, null,
                        this.display[i].getString(), score
                ));
            }
        }

        this.display = display;

        for (ServerPlayerEntity player : this.players) {
            this.sendDisplay(player, display);
        }
    }

    public void set(String[] stringDisplay) {
        List<Text> textDisplay = new ArrayList<>();
        for (String string : stringDisplay) {
            textDisplay.add(new LiteralText(string));
        }
        Text[] display = textDisplay.toArray(new Text[0]);
        if (Arrays.equals(this.display, display)) {
            return;
        }

        // clear old lines
        for (int i = 0; i < this.display.length; i++) {
            int score = display.length - i;
            if (i >= display.length || !this.display[i].equals(display[i])) {
                this.players.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                        ServerScoreboard.UpdateMode.REMOVE, null,
                        this.display[i].getString(), score
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
        ScoreboardObjective objective = this.createDummyObjective(player);

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ADD_OBJECTIVE));
        player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(SIDEBAR_SLOT, objective));

        this.sendDisplay(player, this.display);
    }

    @Override
    public void onRemovePlayer(ServerPlayerEntity player) {
        ScoreboardObjective objective = this.createDummyObjective(player);
        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, REMOVE_OBJECTIVE));
    }

    private void sendDisplay(ServerPlayerEntity player, Text[] lines) {
        for (int i = 0; i < lines.length; i++) {
            Text line = LocalizableText.asLocalizedFor(lines[i],(LocalizationTarget) player);
            int score = lines.length - i;
            player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                    ServerScoreboard.UpdateMode.CHANGE, OBJECTIVE_NAME,
                    line.getString(), score
            ));
        }
    }

    private ScoreboardObjective createDummyObjective(ServerPlayerEntity playerEntity) {
        return new ScoreboardObjective(
                null, OBJECTIVE_NAME,
                ScoreboardCriterion.DUMMY,
                LocalizableText.asLocalizedFor(this.title,(LocalizationTarget) playerEntity),
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
