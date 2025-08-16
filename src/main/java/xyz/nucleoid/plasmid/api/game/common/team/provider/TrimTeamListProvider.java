package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

/**
 * Team provider that trims a list of teams from another provider to a specified size.
 *
 * @author Hugman
 */
public final class TrimTeamListProvider extends TeamListProvider {
    private static final boolean DEFAULT_SHUFFLE = true;

    public static final MapCodec<TrimTeamListProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TeamListProvider.CODEC.fieldOf("provider").forGetter(TrimTeamListProvider::provider),
            IntProvider.POSITIVE_CODEC.fieldOf("size").forGetter(TrimTeamListProvider::size),
            Codec.BOOL.optionalFieldOf("shuffle", DEFAULT_SHUFFLE).forGetter(TrimTeamListProvider::shuffle)
    ).apply(instance, TrimTeamListProvider::new));
    private final TeamListProvider provider;
    private final IntProvider size;
    private final boolean shuffle;

    public TrimTeamListProvider(TeamListProvider provider, IntProvider size, boolean shuffle) {
        this.provider = provider;
        this.size = size;
        this.shuffle = shuffle;
    }

    public TrimTeamListProvider(TeamListProvider provider, int size) {
        this(provider, ConstantIntProvider.create(size), DEFAULT_SHUFFLE);
    }

    public TeamListProvider provider() {
        return provider;
    }

    public IntProvider size() {
        return size;
    }

    public boolean shuffle() {
        return shuffle;
    }

    @Override
    public GameTeamList get(Random random) {
        var list = provider.get(random).list();
        if (shuffle) {
            list = list.stream().sorted((a, b) -> random.nextInt(2) - 1).toList();
        }
        return new GameTeamList(list.subList(0, Math.min(size.get(random), list.size())));
    }

    @Override
    public TeamListProviderType<?> getType() {
        return TeamListProviderType.TRIM;
    }
}
