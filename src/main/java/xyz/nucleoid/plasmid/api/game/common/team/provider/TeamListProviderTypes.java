package xyz.nucleoid.plasmid.api.game.common.team.provider;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;

/**
 * Types of {@link TeamListProvider} that can be registered in the registry ({@link PlasmidRegistries#TEAM_LIST_PROVIDER_TYPE}).
 *
 * @author Hugman
 */
public class TeamListProviderTypes {
    public static final MapCodec<ConstantTeamListProvider> CONSTANT = of("constant", ConstantTeamListProvider.CODEC);
    public static final MapCodec<TrimTeamListProvider> TRIM = of("trim", TrimTeamListProvider.CODEC);
    public static final MapCodec<RandomTeamListProvider> RANDOM = of("random", RandomTeamListProvider.CODEC);
    public static final MapCodec<SizedAlternativesTeamListProvider> SIZED_ALTERNATIVES = of("sized_alternatives", SizedAlternativesTeamListProvider.CODEC);

    private static <T extends TeamListProvider> MapCodec<T> of(String name, MapCodec<T> codec) {
        return of(Identifier.of(Plasmid.ID, name), codec);
    }

    public static <T extends TeamListProvider> MapCodec<T> of(Identifier identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.TEAM_LIST_PROVIDER_TYPE, identifier, codec);
    }
}
