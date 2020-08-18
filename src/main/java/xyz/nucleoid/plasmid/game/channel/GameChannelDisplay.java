package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.text.Text;

public final class GameChannelDisplay {
    private final Text[] lines;

    public GameChannelDisplay(Text[] lines) {
        this.lines = lines;
    }

    public Text[] getLines() {
        return this.lines;
    }
}
