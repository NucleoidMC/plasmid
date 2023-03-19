package xyz.nucleoid.plasmid.game.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

public record RandomGameConfig(RegistryEntryList<GameConfig<?>> games) {
    public static final Codec<RandomGameConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.entryList(GameConfigs.REGISTRY_KEY).fieldOf("games").forGetter(config -> config.games)
    ).apply(i, RandomGameConfig::new));

    @Nullable
    public RegistryEntry<GameConfig<?>> selectGame(Random random) {
        return this.games.getRandom(random).orElse(null);
    }

    public boolean isEmpty() {
        return this.games.size() == 0;
    }
}
