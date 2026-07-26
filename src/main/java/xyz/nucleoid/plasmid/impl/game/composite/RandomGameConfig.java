package xyz.nucleoid.plasmid.impl.game.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

public record RandomGameConfig(HolderSet<GameConfig<?>> games) {
    public static final MapCodec<RandomGameConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameConfig.ENTRY_LIST_CODEC.fieldOf("games").forGetter(config -> config.games)
    ).apply(i, RandomGameConfig::new));

    @Nullable
    public Holder<GameConfig<?>> selectGame(RandomSource random) {
        return this.games.getRandomElement(random).orElse(null);
    }

    public boolean isEmpty() {
        return this.games.size() == 0;
    }
}
