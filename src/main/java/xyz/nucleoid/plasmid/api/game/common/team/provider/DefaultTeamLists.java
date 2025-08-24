package xyz.nucleoid.plasmid.api.game.common.team.provider;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default team lists for various game sizes from 1 to 16 teams.
 *
 * <p>Colors are handpicked and the fewer the teams, the more contrasting are the colors.
 *
 * @author Hugman
 */
public final class DefaultTeamLists {
    private static final GameTeam BLUE = createTeam(DyeColor.BLUE);
    private static final GameTeam GREEN = createTeam(DyeColor.GREEN);
    private static final GameTeam YELLOW = createTeam(DyeColor.YELLOW);
    private static final GameTeam ORANGE = createTeam(DyeColor.ORANGE);
    private static final GameTeam RED = createTeam(DyeColor.RED);
    private static final GameTeam BROWN = createTeam(DyeColor.BROWN);

    private static final GameTeam LIME = createTeam(DyeColor.LIME);
    private static final GameTeam LIGHT_BLUE = createTeam(DyeColor.LIGHT_BLUE);
    private static final GameTeam PINK = createTeam(DyeColor.PINK);
    private static final GameTeam PURPLE = createTeam(DyeColor.PURPLE);

    private static final GameTeam CYAN = createTeam(DyeColor.CYAN);
    private static final GameTeam MAGENTA = createTeam(DyeColor.MAGENTA);

    private static final GameTeam WHITE = createTeam(DyeColor.WHITE);
    private static final GameTeam BLACK = createTeam(DyeColor.BLACK);

    private static final GameTeam GRAY = createTeam(DyeColor.GRAY);
    private static final GameTeam LIGHT_GRAY = createTeam(DyeColor.LIGHT_GRAY);

    private static final GameTeam LIGHT_BLUE_AS_BLUE = createTeam("blue", DyeColor.LIGHT_BLUE);
    private static final GameTeam LIME_AS_GREEN = createTeam("green", DyeColor.LIME);
    private static final GameTeam MAGENTA_AS_PURPLE = createTeam("purple", DyeColor.MAGENTA);

    private static final TeamListProvider RANDOM_TWO = ofLists(List.of(
            List.of(LIGHT_BLUE_AS_BLUE, RED),
            List.of(BLUE, ORANGE),
            List.of(LIME_AS_GREEN, MAGENTA_AS_PURPLE),
            List.of(YELLOW, PURPLE),
            List.of(PINK, CYAN),
            List.of(RED, GREEN),
            List.of(LIGHT_BLUE_AS_BLUE, ORANGE)
    ));

    private static final TeamListProvider RANDOM_FOUR = ofLists(List.of(
            List.of(RED, BLUE, LIME_AS_GREEN, YELLOW),
            List.of(LIME_AS_GREEN, ORANGE, PINK, CYAN)
    ));

    private static final List<GameTeam> POOL_SMALLEST = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, RED);
    private static final List<GameTeam> POOL_FIVE = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, RED, MAGENTA_AS_PURPLE);
    private static final List<GameTeam> POOL_SEVEN = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, ORANGE, RED, MAGENTA_AS_PURPLE, BROWN);
    private static final List<GameTeam> POOL_TEN = List.of(BLUE, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, PURPLE);
    private static final List<GameTeam> POOL_TWELVE = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE);
    private static final List<GameTeam> POOL_FOURTEEN = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE, WHITE, BLACK);
    private static final List<GameTeam> POOL_BIGGEST = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE, WHITE, LIGHT_GRAY, GRAY, BLACK);

    public static final Map<Integer, TeamListProvider> MAP = buildMap();

    private static Map<Integer, TeamListProvider> buildMap() {
        var map = new HashMap<Integer, TeamListProvider>();
        for (int i = 1; i <= 16; i++) {
            map.put(i, getEntry(i));
        }
        return map;
    }

    private static TeamListProvider getEntry(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Team list cannot be empty. Please provide a valid size between 0 and 16.");
        }
        if (size > 16) {
            throw new IllegalArgumentException("Team list cannot be over 16. Please provide a valid size between than 0 and 16.");
        }
        if (size == 2) {
            return RANDOM_TWO;
        }
        if (size == 4) {
            return RANDOM_FOUR;
        }
        if (size > 14) {
            return ofPool(POOL_BIGGEST, size);
        }
        if (size > 12) {
            return ofPool(POOL_FOURTEEN, size);
        }
        if (size > 10) {
            return ofPool(POOL_TWELVE, size);
        }
        if (size > 7) {
            return ofPool(POOL_TEN, size);
        }
        if (size > 5) {
            return ofPool(POOL_SEVEN, size);
        }
        if (size > 4) {
            return ofPool(POOL_FIVE, size);
        }
        return ofPool(POOL_SMALLEST, size);
    }

    private static TeamListProvider ofLists(List<List<GameTeam>> lists) {
        return new RandomTeamListProvider(lists.stream()
                .map(teamList -> (TeamListProvider) new ConstantTeamListProvider(teamList))
                .toList()
        );
    }

    private static TeamListProvider ofPool(List<GameTeam> pool, int size) {
        return new TrimTeamListProvider(new ConstantTeamListProvider(pool), size);
    }

    private static GameTeam createTeam(String name, DyeColor dyeColor) {
        // black text is barely readable, so we use dark gray instead
        var colors = dyeColor == DyeColor.BLACK ?
                new GameTeamConfig.Colors(
                        Formatting.DARK_GRAY,
                        TextColor.fromRgb(dyeColor.getEntityColor()),
                        dyeColor,
                        TextColor.fromRgb(dyeColor.getFireworkColor())
                )
                : GameTeamConfig.Colors.from(dyeColor);

        return new GameTeam(new GameTeamKey(name), GameTeamConfig.builder()
                .setName(Text.translatable("color.minecraft." + name))
                .setColors(colors)
                .build());
    }

    private static GameTeam createTeam(DyeColor dyeColor) {
        return createTeam(dyeColor.getId(), dyeColor);
    }
}
