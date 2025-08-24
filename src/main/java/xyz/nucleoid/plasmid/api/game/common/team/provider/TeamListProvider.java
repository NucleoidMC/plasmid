package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.function.Function;

/**
 * Provides a {@link GameTeamList}.
 *
 * @author Hugman
 * @see TeamListProviderTypes
 */
public interface TeamListProvider {
    Codec<TeamListProvider> CODEC = Codec.either(IntProvider.POSITIVE_CODEC, Codec.either(GameTeamList.CODEC, ((Codec<TeamListProvider>)PlasmidRegistries.TEAM_LIST_PROVIDER_TYPE.getCodec().dispatch(TeamListProvider::getCodec, Function.identity()))
    ).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> provider instanceof ConstantTeamListProvider constant ?
                    Either.left(new GameTeamList(constant.teams())) :
                    Either.right(provider)
    )).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> {
                if (provider instanceof SizedAlternativesTeamListProvider alternatives) {
                    var map = alternatives.map();
                    if (map.equals(DefaultTeamLists.MAP)) {
                        return Either.left(alternatives.size());
                    }
                }
                return Either.right(provider);
            }
    );

    GameTeamList get(Random random);

    MapCodec<? extends TeamListProvider> getCodec();

    static TeamListProvider of(GameTeamList teams) {
        return new ConstantTeamListProvider(teams.list());
    }

    static TeamListProvider of(IntProvider intProvider) {
        return new SizedAlternativesTeamListProvider(intProvider, DefaultTeamLists.MAP);
    }

    static TeamListProvider of(int size) {
        return new SizedAlternativesTeamListProvider(size, DefaultTeamLists.MAP);
    }
}
