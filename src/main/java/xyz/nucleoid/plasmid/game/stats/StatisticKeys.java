package xyz.nucleoid.plasmid.game.stats;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;

/**
 * A set of general {@link StatisticKey}s for minigames to make use of
 *
 * @see StatisticKey
 */
public final class StatisticKeys {
    // Games
    public static final StatisticKey<Integer> GAMES_PLAYED = StatisticKey.intKey(id("games_played"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> GAMES_WON = StatisticKey.intKey(id("games_won"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> GAMES_LOST = StatisticKey.intKey(id("games_lost"), StatisticKey.StorageType.TOTAL);

    // PvP / PvE;
    public static final StatisticKey<Integer> KILLS = StatisticKey.intKey(id("total_kills"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> DEATHS = StatisticKey.intKey(id("deaths"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Double> DAMAGE_TAKEN = StatisticKey.doubleKey(id("damage_taken"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Double> DAMAGE_DEALT = StatisticKey.doubleKey(id("damage_dealt"), StatisticKey.StorageType.TOTAL);

    // Misc;
    public static final StatisticKey<Integer> BLOCKS_BROKEN = StatisticKey.intKey(id("blocks_broken"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> POINTS = StatisticKey.intKey(id("points"), StatisticKey.StorageType.TOTAL);
    public static final StatisticKey<Integer> QUICKEST_TIME = StatisticKey.intKey(id("quickest_time"), StatisticKey.StorageType.MIN);
    public static final StatisticKey<Integer> LONGEST_TIME = StatisticKey.intKey(id("longest_time"), StatisticKey.StorageType.MAX);

    private static Identifier id(String path) {
        return new Identifier(Plasmid.ID, path);
    }
    private StatisticKeys() { }
}
