package xyz.nucleoid.plasmid.api.game.stats;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

/**
 * A set of general {@link StatisticKey}s for minigames to make use of
 *
 * @see StatisticKey
 */
public final class StatisticKeys {
    // Games
    // These are hidden as typically the value for a given game will be 0 or 1
    public static final StatisticKey<Integer> GAMES_PLAYED = StatisticKey.intKey(id("games_played")).hidden(true);
    public static final StatisticKey<Integer> GAMES_WON = StatisticKey.intKey(id("games_won")).hidden(true);
    public static final StatisticKey<Integer> GAMES_LOST = StatisticKey.intKey(id("games_lost")).hidden(true);

    // PvP / PvE;
    public static final StatisticKey<Integer> KILLS = StatisticKey.intKey(id("total_kills"));
    public static final StatisticKey<Integer> DEATHS = StatisticKey.intKey(id("deaths"));
    public static final StatisticKey<Double> DAMAGE_TAKEN = StatisticKey.doubleKey(id("damage_taken"));
    public static final StatisticKey<Double> DAMAGE_DEALT = StatisticKey.doubleKey(id("damage_dealt"));

    // Misc;
    public static final StatisticKey<Integer> BLOCKS_BROKEN = StatisticKey.intKey(id("blocks_broken"));
    public static final StatisticKey<Integer> POINTS = StatisticKey.intKey(id("points"));
    public static final StatisticKey<Integer> QUICKEST_TIME = StatisticKey.intKey(id("quickest_time"));
    public static final StatisticKey<Integer> LONGEST_TIME = StatisticKey.intKey(id("longest_time"));

    private static Identifier id(String path) {
        return Identifier.of(Plasmid.ID, path);
    }
    private StatisticKeys() { }
}
