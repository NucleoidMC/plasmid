package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.List;

public final class RandomTeamListProvider extends TeamListProvider {
    public static final MapCodec<RandomTeamListProvider> CODEC = TeamListProvider.CODEC.listOf().fieldOf("providers").xmap(RandomTeamListProvider::new, RandomTeamListProvider::providers);

    private final List<TeamListProvider> providers;

    public RandomTeamListProvider(List<TeamListProvider> providers) {
        this.providers = providers;
    }

    public List<TeamListProvider> providers() {
        return providers;
    }

    @Override
    public GameTeamList get(Random random) {
        return providers.get(random.nextInt(providers.size())).get(random);
    }

    @Override
    public TeamListProviderType<?> getType() {
        return TeamListProviderType.RANDOM;
    }
}
