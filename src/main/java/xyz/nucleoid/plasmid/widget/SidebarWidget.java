package xyz.nucleoid.plasmid.widget;

import fr.catcore.server.translations.api.LocalizationTarget;
import fr.catcore.server.translations.api.text.LocalizableText;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;

import java.util.Arrays;
import java.util.function.Consumer;

public final class SidebarWidget implements GameWidget {
    private static final int SIDEBAR_SLOT = 1;
    private static final int ADD_OBJECTIVE = 0;
    private static final int REMOVE_OBJECTIVE = 1;

    private static final String OBJECTIVE_NAME = Plasmid.ID + ":sidebar";

    private static final char[] AVAILABLE_FORMATTING_CODES;

    static {
        CharSet vanillaFormattingCodes = new CharOpenHashSet();
        for (Formatting formatting : Formatting.values()) {
            vanillaFormattingCodes.add(formatting.toString().charAt(1));
        }

        CharList availableFormattingCodes = new CharArrayList();
        for (char code = 'a'; code <= 'z'; code++) {
            if (!vanillaFormattingCodes.contains(code)) {
                availableFormattingCodes.add(code);
            }
        }

        AVAILABLE_FORMATTING_CODES = availableFormattingCodes.toCharArray();
    }

    private final MutablePlayerSet players;
    private final Text title;

    private final Content content = new Content();

    public SidebarWidget(GameSpace gameSpace, Text title) {
        this(gameSpace.getServer(), title);
    }

    public SidebarWidget(MinecraftServer server, Text title) {
        this.players = new MutablePlayerSet(server);
        this.title = title;
    }

    public void set(Consumer<Content> writer) {
        writer.accept(this.content);
        this.content.flush();
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        this.players.add(player);

        ScoreboardObjective objective = this.createDummyObjective(player);

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ADD_OBJECTIVE));
        player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(SIDEBAR_SLOT, objective));

        this.content.sendTo(player);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        this.players.remove(player);

        ScoreboardObjective objective = this.createDummyObjective(player);
        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, REMOVE_OBJECTIVE));
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

    private static String prefixLine(int i, String line) {
        return "\u00a7" + AVAILABLE_FORMATTING_CODES[i] + line;
    }

    public class Content {
        private Object[] lines = new Object[16];
        private Object[] lastLines = new Object[16];

        private int writeIndex;
        private boolean changed;

        private int lastLength;

        public Content writeLine(String line) {
            return this.writeRawLine(line);
        }

        public Content writeTranslated(String key, Object... args) {
            return this.writeRawLine(new TranslatableText(key, args));
        }

        private Content writeRawLine(Object line) {
            int writeIndex = this.writeIndex++;
            if (writeIndex >= this.lines.length) {
                return this;
            }

            this.lines[writeIndex] = line;
            if (!line.equals(this.lastLines[writeIndex])) {
                this.changed = true;
            }

            return this;
        }

        void flush() {
            MutablePlayerSet players = SidebarWidget.this.players;

            int length = this.writeIndex;

            if (length < this.lastLength) {
                // clear any lines that got removed
                for (int i = length; i < this.lastLength; i++) {
                    for (ServerPlayerEntity player : players) {
                        this.sendRemoveLine(player, i);
                    }
                }
            }

            if (this.changed) {
                // update any lines that have changed
                for (int i = 0; i < length; i++) {
                    if (this.lines[i].equals(this.lastLines[i])) {
                        continue;
                    }

                    int score = length - i;
                    for (ServerPlayerEntity player : players) {
                        if (i < this.lastLength) {
                            this.sendRemoveLine(player, i);
                        }
                        this.sendUpdateLine(player, this.getLineForPlayer(this.lines[i], i, player), score);
                    }
                }
            }

            Object[] swap = this.lastLines;
            Arrays.fill(swap, null);

            this.lastLines = this.lines;
            this.lines = swap;
            this.lastLength = length;

            this.writeIndex = 0;
            this.changed = false;
        }

        void sendTo(ServerPlayerEntity player) {
            int length = this.lastLength;
            for (int i = 0; i < length; i++) {
                int score = length - i;
                this.sendRemoveLine(player, i);
                this.sendUpdateLine(player, this.getLineForPlayer(this.lastLines[i], i, player), score);
            }
        }

        void sendUpdateLine(ServerPlayerEntity player, String line, int score) {
            player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                    ServerScoreboard.UpdateMode.CHANGE, OBJECTIVE_NAME,
                    line, score
            ));
        }

        void sendRemoveLine(ServerPlayerEntity player, int index) {
            player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(
                    ServerScoreboard.UpdateMode.REMOVE, null,
                    this.getLineForPlayer(this.lastLines[index], index, player), -1
            ));
        }

        String getLineForPlayer(Object line, int index, ServerPlayerEntity player) {
            String text;
            if (line instanceof String) {
                text = (String) line;
            } else if (line instanceof Text) {
                text = LocalizableText.asLocalizedFor((Text) line, (LocalizationTarget) player).getString();
            } else {
                text = line.toString();
            }
            return prefixLine(index, text);
        }
    }
}
