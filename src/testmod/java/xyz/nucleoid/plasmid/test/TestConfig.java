package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

import java.util.Optional;

public record TestConfig(int integer, BlockState state, Optional<RegistryEntryList<Item>> items) {
    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.INT.optionalFieldOf("integer", 0).forGetter(TestConfig::integer),
                BlockState.CODEC.optionalFieldOf("state", Blocks.BLUE_STAINED_GLASS.getDefaultState()).forGetter(TestConfig::state),
                RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("items").forGetter(TestConfig::items)
            ).apply(instance, TestConfig::new));
}
