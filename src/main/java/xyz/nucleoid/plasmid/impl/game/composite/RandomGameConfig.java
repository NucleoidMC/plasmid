package xyz.nucleoid.plasmid.impl.game.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

public record RandomGameConfig(RegistryEntryList<GameConfig<?>> games) {
    public static final MapCodec<RandomGameConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GameConfig.ENTRY_LIST_CODEC.fieldOf("games").forGetter(config -> config.games)
    ).apply(i, RandomGameConfig::new));

    @Nullable
    public RegistryEntry<GameConfig<?>> selectGame(Random random) {
        return this.games.getRandom(random).orElse(null);
    }

    public boolean isEmpty() {
        return this.games.size() == 0;
    }
}
