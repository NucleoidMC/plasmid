package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;

public class GameAttachment<T> {
    private final Identifier id;

    private GameAttachment(Identifier id) {
        this.id = id;
    }

    public static <T> GameAttachment<T> create(Identifier id) {
        return new GameAttachment<>(id);
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
