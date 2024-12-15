package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

import java.util.Optional;

public record TestConfig(int integer, WaitingLobbyConfig players, BlockState state, Optional<RegistryEntryList<Item>> items, int teamCount) {
    public static final MapCodec<TestConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("integer", 0).forGetter(TestConfig::integer),
            WaitingLobbyConfig.CODEC.optionalFieldOf("players", new WaitingLobbyConfig(1, 99)).forGetter(TestConfig::players),
            BlockState.CODEC.optionalFieldOf("state", Blocks.BLUE_STAINED_GLASS.getDefaultState()).forGetter(TestConfig::state),
            RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("items").forGetter(TestConfig::items),
            Codec.INT.optionalFieldOf("team_count", 0).forGetter(TestConfig::teamCount)
    ).apply(i, TestConfig::new));
}
