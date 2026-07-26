package xyz.nucleoid.plasmid.test;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.scores.Team;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig.Colors;

import static org.junit.jupiter.api.Assertions.*;

public class GameTeamTests {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
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
                Component.literal("Team Name"),
                Colors.from(DyeColor.BLUE),
                true,
                Team.CollisionRule.NEVER,
                Team.Visibility.NEVER,
                Component.empty(),
                Component.empty()
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
                Component.translatable("color.minecraft.red"),
                Colors.from(DyeColor.RED),
                false,
                Team.CollisionRule.PUSH_OTHER_TEAMS,
                Team.Visibility.HIDE_FOR_OTHER_TEAMS,
                Component.literal("Prefix"),
                Component.literal("Suffix")
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
                Component.literal("Team"),
                Colors.NONE,
                false,
                Team.CollisionRule.ALWAYS,
                Team.Visibility.HIDE_FOR_OWN_TEAM,
                Component.empty(),
                Component.empty()
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
