package xyz.nucleoid.plasmid.game.channel.on_demand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelConfig;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;

public final class OnDemandChannelConfig implements GameChannelConfig {
    public static final Codec<OnDemandChannelConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("game_id").forGetter(channel -> channel.gameId),
                Codec.BOOL.optionalFieldOf("continuous", false).forGetter(channel -> channel.continuous)
        ).apply(instance, OnDemandChannelConfig::new);
    });

    private final Identifier gameId;
    private final boolean continuous;

    public OnDemandChannelConfig(Identifier gameId, boolean continuous) {
        this.gameId = gameId;
        this.continuous = continuous;
    }

    @Override
    public GameChannelBackend createBackend(GameChannelMembers members) {
        if (this.continuous) {
            return new ContinuousOnDemandChannelBackend(this.gameId, members);
        } else {
            return new OnDemandChannelBackend(this.gameId, members);
        }
    }

    @Override
    public Codec<? extends GameChannelConfig> getCodec() {
        return CODEC;
    }
}
