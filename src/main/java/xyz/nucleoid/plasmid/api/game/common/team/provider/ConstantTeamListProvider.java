package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * Team provider that provides a constant list of teams.
 *
 * <p>This is the most basic team provider and can be used inline for any team list provider.
 *
 * @author Hugman
 */
public record ConstantTeamListProvider(List<GameTeam> teams) implements TeamListProvider {
    public static final MapCodec<ConstantTeamListProvider> CODEC = GameTeam.CODEC.listOf().fieldOf("teams").xmap(ConstantTeamListProvider::new, ConstantTeamListProvider::teams);

    @Override
    public GameTeamList get(RandomSource random) {
        return new GameTeamList(teams);
    }

    @Override
    public MapCodec<ConstantTeamListProvider> getCodec() {
        return CODEC;
    }
}
