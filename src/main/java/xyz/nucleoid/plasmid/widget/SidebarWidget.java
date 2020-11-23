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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.util.TranslatableString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SidebarWidget implements GameWidget {
    private static final int SIDEBAR_SLOT = 1;
    private static final int ADD_OBJECTIVE = 0;
    private static final int REMOVE_OBJECTIVE = 1;

    private static final String OBJECTIVE_NAME = Plasmid.ID + ":sidebar";

    private final MutablePlayerSet players;
    private final Text title;

    private String[] display = new String[0];

    public SidebarWidget(GameSpace gameSpace, Text title) {
        this.players = new MutablePlayerSet(gameSpace.getServer());
        this.title = title;
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

    public void set(TranslatableString[] display) {
        List<String> stringList = new ArrayList<>();

        for (TranslatableString text : display) {
            stringList.add(text.getDefaultTranslation());
        }

        String[] stringDisplay = stringList.toArray(new String[0]);

        if (Arrays.equals(this.display, stringDisplay)) {
            return;
        }

        // clear old lines
        for (int i = 0; i < this.display.length; i++) {
            int score = display.length - i;
            if (i >= display.length || !this.display[i].equals(display[i].getDefaultTranslation())) {
                this.players.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                        ServerScoreboard.UpdateMode.REMOVE, null,
                        this.display[i], score
                ));
            }
        }

        this.display = stringDisplay;

        for (ServerPlayerEntity player : this.players) {
            this.sendDisplay(player, display);
        }
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        this.players.add(player);

        ScoreboardObjective objective = this.createDummyObjective();
        if (this.title instanceof TranslatableText) {
            objective = this.createDummyObjective(player);
        }

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ADD_OBJECTIVE));
        player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(SIDEBAR_SLOT, objective));

        this.sendDisplay(player, this.display);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        this.players.remove(player);

        ScoreboardObjective objective = this.createDummyObjective();
        if (this.title instanceof TranslatableText) {
            objective = this.createDummyObjective(player);
        }
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

    private void sendDisplay(ServerPlayerEntity player, TranslatableString[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].localizeFor((LocalizationTarget) player);
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

    private ScoreboardObjective createDummyObjective(ServerPlayerEntity player) {
        return new ScoreboardObjective(
                null, OBJECTIVE_NAME,
                ScoreboardCriterion.DUMMY,
                LocalizableText.asLocalizedFor(this.title, (LocalizationTarget) player),
                ScoreboardCriterion.RenderType.INTEGER
        );
    }

    @Override
    public void close() {
        for (ServerPlayerEntity player : this.players) {
            this.removePlayer(player);
        }
    }
}
