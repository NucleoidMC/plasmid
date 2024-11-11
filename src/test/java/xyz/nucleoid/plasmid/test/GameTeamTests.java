package xyz.nucleoid.plasmid.test;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig.Colors;

import static org.junit.jupiter.api.Assertions.*;

public class GameTeamTests {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    public void testConfigParsing() {
        var json = """
            {
                "name": {
                    "text": "Team Name"
                },
                "color": "blue",
                "friendly_fire": true,
                "collision": "never",
                "name_tag_visibility": "never"
            }
        """;

        var expected = new GameTeamConfig(
                Text.literal("Team Name"),
                Colors.from(DyeColor.BLUE),
                true,
                AbstractTeam.CollisionRule.NEVER,
                AbstractTeam.VisibilityRule.NEVER,
                Text.empty(),
                Text.empty()
        );

        assertParsedEquals(json, expected, GameTeamConfig.CODEC);
    }

    @Test
    public void testConfigParsingWithoutName() {
        var json = """
            {
                "color": "red",
                "friendly_fire": false,
                "collision": "pushOtherTeams",
                "name_tag_visibility": "hideForOtherTeams",
                "prefix": {
                    "text": "Prefix"
                },
                "suffix": {
                    "text": "Suffix"
                }
            }
        """;

        var expected = new GameTeamConfig(
                Text.translatable("color.minecraft.red"),
                Colors.from(DyeColor.RED),
                false,
                AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS,
                AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS,
                Text.literal("Prefix"),
                Text.literal("Suffix")
        );

        assertParsedEquals(json, expected, GameTeamConfig.CODEC);
    }

    @Test
    public void testConfigParsingWithoutColor() {
        var json = """
            {
                "friendly_fire": false,
                "collision": "always",
                "name_tag_visibility": "hideForOwnTeam"
            }
        """;

        var expected = new GameTeamConfig(
                Text.literal("Team"),
                Colors.NONE,
                false,
                AbstractTeam.CollisionRule.ALWAYS,
                AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM,
                Text.empty(),
                Text.empty()
        );

        assertParsedEquals(json, expected, GameTeamConfig.CODEC);
    }

    private static <T> void assertParsedEquals(String json, T expected, Codec<T> codec) {
        var actual = parse(json, codec);
        assertEquals(expected, actual);
    }

    private static <T> T parse(String json, Codec<T> codec) {
        var element = JsonParser.parseString(json);
        var result = codec.decode(JsonOps.INSTANCE, element);

        return result.getOrThrow().getFirst();
    }
}
