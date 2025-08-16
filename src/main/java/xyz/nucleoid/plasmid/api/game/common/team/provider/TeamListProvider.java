package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

/**
 * Provides a {@link GameTeamList}.
 *
 * @see TeamListProviderType
 *
 * @author Hugman
 */
public abstract class TeamListProvider {
    private static final Codec<TeamListProvider> BASE_CODEC = PlasmidRegistries.TEAM_LIST_PROVIDER_TYPE.getCodec().dispatch(TeamListProvider::getType, TeamListProviderType::codec);
    private static final Codec<TeamListProvider> INLINE_LIST_CODEC = Codec.either(GameTeamList.CODEC, BASE_CODEC).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> provider.getType() == TeamListProviderType.CONSTANT ?
                    Either.left(new GameTeamList(((ConstantTeamListProvider) provider).teams())) :
                    Either.right(provider)
    );
    public static final Codec<TeamListProvider> CODEC = Codec.either(IntProvider.POSITIVE_CODEC, INLINE_LIST_CODEC).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> {
                if (provider.getType() == TeamListProviderType.SIZED_ALTERNATIVES) {
                    var map = ((SizedAlternativesTeamListProvider) provider).map();
                    if (map.equals(DefaultTeamLists.MAP)) {
                        return Either.left(((SizedAlternativesTeamListProvider) provider).size());
                    }
                }
                return Either.right(provider);
            }
    );

    public abstract GameTeamList get(Random random);

    public abstract TeamListProviderType<?> getType();

    public static TeamListProvider of(GameTeamList teams) {
        return new ConstantTeamListProvider(teams.list());
    }

    public static TeamListProvider of(IntProvider intProvider) {
        return new SizedAlternativesTeamListProvider(intProvider, DefaultTeamLists.MAP);
    }

    public static TeamListProvider of(int size) {
        return new SizedAlternativesTeamListProvider(size, DefaultTeamLists.MAP);
    }
}
