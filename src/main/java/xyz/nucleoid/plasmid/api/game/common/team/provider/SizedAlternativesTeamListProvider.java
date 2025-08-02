package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.Map;

public final class SizedAlternativesTeamListProvider extends TeamListProvider {
    public static final MapCodec<SizedAlternativesTeamListProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProvider.createValidatingCodec(1, 16).fieldOf("size").forGetter(SizedAlternativesTeamListProvider::size),
            Codec.unboundedMap(Codec.INT, TeamListProvider.CODEC).optionalFieldOf("alternatives", DefaultTeamAlternatives.MAP).forGetter(SizedAlternativesTeamListProvider::map)
    ).apply(instance, SizedAlternativesTeamListProvider::new));
    private final IntProvider size;
    private final Map<Integer, TeamListProvider> map;

    public SizedAlternativesTeamListProvider(IntProvider size, Map<Integer, TeamListProvider> map) {
        this.size = size;
        this.map = map;
    }

    @Override
    public GameTeamList get(Random random) {
        var size = this.size.get(random);
        return new TrimTeamListProvider(this.map.get(size), size).get(random);
    }

    @Override
    public TeamListProviderType<?> getType() {
        return TeamListProviderType.SIZED_ALTERNATIVES;
    }

    public IntProvider size() {
        return size;
    }

    public Map<Integer, TeamListProvider> map() {
        return map;
    }
}
