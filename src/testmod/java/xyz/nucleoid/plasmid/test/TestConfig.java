package xyz.nucleoid.plasmid.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;

import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record TestConfig(int integer, WaitingLobbyConfig players, BlockState state, Optional<HolderSet<Item>> items, Optional<TeamListProvider> teams) {
    public static final MapCodec<TestConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("integer", 0).forGetter(TestConfig::integer),
            WaitingLobbyConfig.CODEC.optionalFieldOf("players", new WaitingLobbyConfig(1, 99)).forGetter(TestConfig::players),
            BlockState.CODEC.optionalFieldOf("state", Blocks.STAINED_GLASS.blue().defaultBlockState()).forGetter(TestConfig::state),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(TestConfig::items),
            TeamListProvider.CODEC.optionalFieldOf("teams").forGetter(TestConfig::teams)
    ).apply(i, TestConfig::new));
}
