package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.Map;

/**
 * Teams provider that provides other providers based on a set size. That size may be fixed or randomized using an {@link IntProvider}.
 *
 * @author Hugman
 */
public record SizedAlternativesTeamListProvider(
        IntProvider size,
        Map<Integer, TeamListProvider> map
) implements TeamListProvider {
    private static final Map<Integer, TeamListProvider> DEFAULT_ALTERNATIVES = DefaultTeamLists.MAP;

    public static final MapCodec<SizedAlternativesTeamListProvider> CODEC = RecordCodecBuilder.<SizedAlternativesTeamListProvider>mapCodec(instance -> instance.group(
                            IntProvider.POSITIVE_CODEC.fieldOf("size").forGetter(SizedAlternativesTeamListProvider::size),
                            Codec.unboundedMap(Codec.INT, TeamListProvider.CODEC).validate(SizedAlternativesTeamListProvider::validateAlternatives).optionalFieldOf("alternatives", DEFAULT_ALTERNATIVES).forGetter(SizedAlternativesTeamListProvider::map)
                    ).apply(instance, SizedAlternativesTeamListProvider::new)
            )
            .validate(SizedAlternativesTeamListProvider::validate);

    public SizedAlternativesTeamListProvider(int size, Map<Integer, TeamListProvider> map) {
        this(ConstantIntProvider.create(size), map);
    }

    public SizedAlternativesTeamListProvider(IntProvider size) {
        this(size, DEFAULT_ALTERNATIVES);
    }

    public SizedAlternativesTeamListProvider(int size) {
        this(ConstantIntProvider.create(size));
    }

    private static DataResult<SizedAlternativesTeamListProvider> validate(SizedAlternativesTeamListProvider provider) {
        return provider.size.getMax() <= provider.map.size() ? DataResult.success(provider) : DataResult.error(() -> "The size provider cannot provide more teams than the maximum ");
    }

    private static DataResult<Map<Integer, TeamListProvider>> validateAlternatives(Map<Integer, TeamListProvider> map) {
        // check if continuous
        for (int i = 1; i <= map.size(); i++) {
            if (!map.containsKey(i)) {
                return DataResult.error(() -> "The alternatives must contain a team provider for every size from 1 to " + map.size());
            }
        }
        return DataResult.success(map);
    }

    @Override
    public GameTeamList get(Random random) {
        return this.map.get(this.size.get(random)).get(random);
    }

    @Override
    public MapCodec<? extends TeamListProvider> getCodec() {
        return CODEC;
    }
}
