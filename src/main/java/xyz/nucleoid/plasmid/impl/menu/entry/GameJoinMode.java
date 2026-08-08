package xyz.nucleoid.plasmid.impl.menu.entry;

import com.mojang.serialization.Codec;

/**
 * How a {@code plasmid:game} entry turns a click into a game space.
 */
public enum GameJoinMode {
    /**
     * Join an open game of this config, opening one only when none can be joined.
     */
    CONCURRENT("concurrent"),
    /**
     * One shared game, reused for as long as it lives.
     */
    SINGLE("single"),
    /**
     * Always open a fresh game.
     */
    NEW("new");

    public static final Codec<GameJoinMode> CODEC = Codec.stringResolver(mode -> mode.name, GameJoinMode::byName);

    private final String name;

    GameJoinMode(String name) {
        this.name = name;
    }

    private static GameJoinMode byName(String name) {
        for (var mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }

        return null;
    }
}
