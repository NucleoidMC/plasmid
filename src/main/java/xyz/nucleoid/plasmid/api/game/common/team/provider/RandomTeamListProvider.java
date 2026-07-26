package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * Team provider that randomly selects a team list from a list of providers.
 *
 * @author Hugman
 */
public record RandomTeamListProvider(List<TeamListProvider> providers) implements TeamListProvider {
    public static final MapCodec<RandomTeamListProvider> CODEC = TeamListProvider.CODEC.listOf().fieldOf("providers").xmap(RandomTeamListProvider::new, RandomTeamListProvider::providers);

    @Override
    public GameTeamList get(RandomSource random) {
        return providers.get(random.nextInt(providers.size())).get(random);
    }

    @Override
    public MapCodec<? extends TeamListProvider> getCodec() {
        return CODEC;
    }
}
